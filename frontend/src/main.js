import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'

const app = createApp(App)

// FIX: Pinia PHẢI được use() trước router
// để các store có thể được inject khi router guards chạy
app.use(createPinia())
app.use(router)
app.use(Toast, { timeout: 3000, position: 'top-right', closeOnClick: true })

app.mount('#app')
