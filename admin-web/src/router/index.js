import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '@/views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  }
]

const router = new VueRouter({
  // 默认 hash 模式：刷新页面不会 404，静态部署也不需要额外的 fallback 配置
  mode: 'hash',
  routes
})

export default router
