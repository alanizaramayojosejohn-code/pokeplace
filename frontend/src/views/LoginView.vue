<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTokenClient, decodeCredential } from 'vue3-google-signin'
import { GoogleSignInButton } from 'vue3-google-signin'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleGoogleSuccess = async (response: any) => {
  try {
    await authStore.googleLogin(response.credential)
    router.push('/dashboard')
  } catch (e) {
    error.value = 'Google login failed. Contact your administrator.'
  }
}

const handleGoogleError = () => {
  error.value = 'Google login failed. Try again.'
}

const { isReady, login: googleSignIn } = useTokenClient({
  onSuccess: handleGoogleSuccess,
  onError: handleGoogleError,
})
async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/dashboard')
  } catch (e) {
    error.value = 'Invalid email or password'
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
              <input v-model="password" type="password" placeholder="••••••••" required />
            </div>

            <p v-if="error" class="error">⚠ {{ error }}</p>

            <button type="submit" :disabled="loading">
              <span v-if="!loading">Ingresar →</span>
              <span v-else class="spinner"></span>
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
