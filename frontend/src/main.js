import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primeuix/themes/aura'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import 'primeicons/primeicons.css'
import './assets/styles/main.css'
import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from './services/api'

setUnauthorizedHandler(() => router.push('/login'))

createApp(App)
  .use(router)
  .use(PrimeVue, { theme: { preset: Aura } })
  .use(ToastService)
  .use(ConfirmationService)
  .mount('#app')
