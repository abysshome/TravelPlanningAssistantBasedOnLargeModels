import './assets/main.css'

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import Top from './Top.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js';  // 引入 jQuery

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
app.use(ElementPlus)
app.mount('#app')

const top = createApp(Top)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    top.component(key, component)
  }
top.use(ElementPlus)
top.mount('#top')

