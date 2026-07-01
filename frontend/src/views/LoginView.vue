<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { GoogleSignInButton } from 'vue3-google-signin'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const error = ref('')
const loading = ref(false)

const MAX_ATTEMPTS = 5
const LOCKOUT_KEY = 'login_locked_until'
const attempts = ref(0)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

function getLockoutRemaining(): number {
  const until = localStorage.getItem(LOCKOUT_KEY)
  if (!until) return 0
  const remaining = Math.ceil((Number(until) - Date.now()) / 1000)
  return remaining > 0 ? remaining : 0
}

function startCountdown(seconds: number) {
  countdown.value = seconds
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
      localStorage.removeItem(LOCKOUT_KEY)
      attempts.value = 0
    }
  }, 1000)
}

// Restaurar lockout si recarga la página
const remaining = getLockoutRemaining()
if (remaining > 0) startCountdown(remaining)

const isLocked = computed(() => countdown.value > 0)
const countdownLabel = computed(() => {
  const m = Math.floor(countdown.value / 60)
  const s = countdown.value % 60
  return m > 0 ? `${m}:${String(s).padStart(2, '0')} min` : `${s}s`
})

onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer) })

const handleGoogleSuccess = async (response: any) => {
  try {
    await authStore.googleLogin(response.credential)
    router.push('/dashboard')
  } catch {
    error.value = 'Google login fallido. Contacta al administrador.'
  }
}

const handleGoogleError = () => {
  error.value = 'Google login fallido. Intenta de nuevo.'
}

async function handleLogin() {
  if (isLocked.value) return
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    attempts.value = 0
    localStorage.removeItem(LOCKOUT_KEY)
    router.push('/dashboard')
  } catch (e: any) {
    const status = e?.response?.status
    const message = e?.response?.data?.message ?? ''
    const secondsUntilUnlock = e?.response?.data?.secondsUntilUnlock

    if (status === 429 || secondsUntilUnlock) {
      const secs = secondsUntilUnlock ?? getLockoutRemaining()
      localStorage.setItem(LOCKOUT_KEY, String(Date.now() + secs * 1000))
      startCountdown(secs)
      error.value = `Demasiados intentos fallidos. Espera ${countdownLabel.value}.`
    } else {
      attempts.value = Math.min(attempts.value + 1, MAX_ATTEMPTS)
      const remaining = MAX_ATTEMPTS - attempts.value
      if (remaining > 0) {
        error.value = `Email o contraseña incorrectos. ${remaining} intento${remaining !== 1 ? 's' : ''} restante${remaining !== 1 ? 's' : ''}.`
      } else {
        error.value = message || 'Cuenta bloqueada temporalmente.'
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <body>
    <div class="root">
      <div class="brand-panel">
        <div class="brand-content">
          <div>
            <img src="../assets/Logo.webp" alt="Logo" class="logo-mark" />
          </div>
          <h1 class="brand-name">Poke<span>Place</span></h1>
          <p class="brand-tagline">Sistema de Ventas</p>
        </div>
        <div class="pattern"></div>
      </div>

      <div class="form-panel">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>Bienvenido de vuelta</h2>
            <p>Inicia sesión para continuar</p>
          </div>

          <form @submit.prevent="handleLogin">
            <div class="field">
              <label>Email</label>
              <input v-model="email" type="email" placeholder="you@pokeplace.com" required />
            </div>

            <div class="field">
              <label>Contraseña</label>
              <div class="password-wrapper">
                <input
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  required
                />
                <button
                  type="button"
                  class="toggle-password"
                  @click="showPassword = !showPassword"
                  :title="showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                >
                  {{ showPassword ? '🙈' : '👁' }}
                </button>
              </div>
            </div>

            <p v-if="error" class="error">⚠ {{ error }}</p>

            <div v-if="isLocked" class="lockout-banner">
              🔒 Cuenta bloqueada — espera <strong>{{ countdownLabel }}</strong> para intentar de nuevo.
            </div>

            <button type="submit" :disabled="loading || isLocked">
              <span v-if="loading" class="spinner"></span>
              <span v-else-if="isLocked">🔒 Bloqueado ({{ countdownLabel }})</span>
              <span v-else>Ingresar →</span>
            </button>
          </form>
          <div class="divider">
            <span>o</span>
          </div>

          <GoogleSignInButton
            @success="handleGoogleSuccess"
            @error="handleGoogleError"
            width="380"
          />
        </div>
      </div>
    </div>
  </body>
</template>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.root {
  display: flex;
  min-height: 100vh;
  min-width: 100vw;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.brand-panel {
  position: relative;
  width: 45%;
  background: #111;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: 2rem;
}

.logo-mark {
  width: 72px;
  height: 72px;
  background: #e02020;
  color: white;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 2.5rem;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  border-radius: 4px;
}

.brand-name {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 3rem;
  font-weight: 900;
  letter-spacing: -1px;
  line-height: 1;
  margin-bottom: 1rem;
}

.brand-name span {
  color: #e02020;
}

.brand-tagline {
  color: #888;
  font-size: 0.85rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.pattern {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 40px,
    rgba(255, 255, 255, 0.02) 40px,
    rgba(255, 255, 255, 0.02) 41px
  );
}

.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 2rem;
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 2.5rem;
}

.form-header h2 {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #111;
  margin-bottom: 0.4rem;
}

.form-header p {
  color: #888;
  font-size: 0.95rem;
}

.field {
  margin-bottom: 1.25rem;
}

label {
  display: block;
  font-size: 0.8rem;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #111;
  margin-bottom: 0.5rem;
}

input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1.5px solid #e0e0e0;
  border-radius: 4px;
  font-size: 0.95rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #111;
  transition: border-color 0.2s;
  outline: none;
}

input:focus {
  border-color: #e02020;
}

button {
  width: 100%;
  padding: 0.9rem;
  background: #e02020;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  cursor: pointer;
  margin-top: 0.5rem;
  transition:
    background 0.2s,
    transform 0.1s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

button:hover:not(:disabled) {
  background: #c01010;
}
button:active:not(:disabled) {
  transform: scale(0.99);
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e02020;
  font-size: 0.85rem;
  margin: 0.75rem 0;
  padding: 0.6rem 0.8rem;
  background: #fff0f0;
  border-radius: 4px;
  border-left: 3px solid #e02020;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .root {
    flex-direction: column;
  }
  .brand-panel {
    width: 100%;
    padding: 3rem 2rem;
  }
  .brand-name {
    font-size: 2.2rem;
  }
  .logo-mark {
    width: 56px;
    height: 56px;
    font-size: 2rem;
  }
}

.divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1.25rem 0;
  color: #aaa;
  font-size: 0.85rem;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e0e0e0;
}
.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.password-wrapper input {
  padding-right: 2.8rem;
}
.toggle-password {
  position: absolute;
  right: 0.75rem;
  background: none;
  border: none;
  width: auto;
  min-height: auto;
  padding: 0;
  margin: 0;
  font-size: 1rem;
  cursor: pointer;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}
.toggle-password:hover {
  color: #111;
  background: none;
}

.lockout-banner {
  background: #fff7ed;
  border-left: 3px solid #f97316;
  color: #9a3412;
  padding: 0.6rem 0.8rem;
  border-radius: 4px;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

.google-btn {
  width: 100%;
  padding: 0.8rem;
  background: white;
  color: #111;
  border: 1.5px solid #e0e0e0;
  border-radius: 4px;
  font-size: 0.95rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  transition:
    border-color 0.2s,
    background 0.2s;
  margin-top: 0;
}
.google-btn:hover:not(:disabled) {
  border-color: #111;
  background: #fafafa;
}
.google-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
