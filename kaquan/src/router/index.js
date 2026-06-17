import { createRouter, createWebHistory } from 'vue-router'
import http from '@/utils/http'

const routes = [
  { path: '/', redirect: '/index' },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/index', component: () => import('@/views/Index.vue') },
  { path: '/cate', component: () => import('@/views/Cate.vue') },
  { path: '/order', component: () => import('@/views/Order.vue') },
  { path: '/mine', component: () => import('@/views/Mine.vue') },
  { path: '/adminLogin', component: () => import('@/views/admin/AdminLogin.vue') },
  { path: '/admin', component: () => import('@/views/admin/AdminIndex.vue') },
  // 新增商品详情页和确认订单页路由
  { path: '/goodsDetail/:id', component: () => import('@/views/GoodsDetail.vue') },
  { path: '/confirmOrder', component: () => import('@/views/ConfirmOrder.vue') },
  { path: '/recharge', component: () => import('@/views/Recharge.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })

// 全局前置守卫（修正版）
router.beforeEach(async (to, from, next) => {
  // 1. 免登录页面（直接放行）
  const publicPages = ['/index', '/cate', '/login', '/goodsDetail/:id']
  // 注意：动态路由匹配需要对比路径前缀
  if (to.path === '/index' || to.path === '/cate' || to.path === '/login' || to.path.startsWith('/goodsDetail/')) {
    return next()
  }

  // 2. 确认订单页需要登录
  if (to.path === '/confirmOrder') {
    try {
      const res = await http.post('/user/getLogin')
      if (res.data.code !== 200) return next('/login')
    } catch {
      return next('/login')
    }
    return next()
  }

  // 3. 我的订单、我的页面、充值页面需要登录
  if (to.path === '/order' || to.path === '/mine' || to.path === '/recharge') {
    try {
      const res = await http.post('/user/getLogin')
      if (res.data.code !== 200) return next('/login')
    } catch {
      return next('/login')
    }
    return next()
  }

  // 4. 后台管理页面（除登录页外）需要管理员登录
  if (to.path.startsWith('/admin') && to.path !== '/adminLogin') {
    try {
      const res = await http.post('/admin/check')
      if (res.data.code !== 200) return next('/adminLogin')
    } catch {
      return next('/adminLogin')
    }
    return next()
  }

  // 5. 其他页面默认放行
  next()
})

export default router