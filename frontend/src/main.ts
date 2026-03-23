import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import GoogleSignInPlugin from 'vue3-google-signin'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(GoogleSignInPlugin, {
  clientId: '47543421099-t1dvfnfkcdk4gbu6ndmr6r69ao07mdko.apps.googleusercontent.com',
})

app.mount('#app')
