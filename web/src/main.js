import {createApp} from 'vue'
import App from './App.vue'
import './assets/tailwind.css'
import Router from './router/index'

createApp(App).use(Router).mount('#app')
