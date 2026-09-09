import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useTokenStore } from '@/stores/token.js'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      children: [
        {
          path: '/',
          name: 'produceList',
          component: () => import('../views/ProduceView.vue'),
        },
        {
          path: 'produce/:id',
          name: 'produceDetail',
          component: () => import('../views/ProduceDetailView.vue'),
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('../views/CartView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/OrdersView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'checkout',
          name: 'checkout',
          component: () => import('../views/CheckoutView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'coupon-center',
          name: 'couponCenter',
          component: () => import('../views/CouponCenterView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'package',
          name: 'packageList',
          component: () => import('../views/PackageListView.vue'),
        },
        {
          path: 'package/:id',
          name: 'packageDetail',
          component: () => import('../views/PackageDetailView.vue'),
        },
        {
          path: 'search',
          name: 'search',
          component: () => import('../views/SearchView.vue'),
        },
      ],
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/forgot-password',
      name: 'forgotPassword',
      component: () => import('../views/ForgotPasswordView.vue'),
    },
    // 支付成功回调重定向到订单页面
    {
      path: '/paySuccess',
      redirect: '/orders'
    },
    // 旧路径兼容
    {
      path: '/order',
      redirect: '/orders'
    },
  ],
})

// 路由守卫：未登录时跳转登录页
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const tokenStore = useTokenStore()
    if (!tokenStore.token) {
      ElMessage.warning('请先登录')
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

export default router
