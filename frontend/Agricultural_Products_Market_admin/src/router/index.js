import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('../views/LayoutView.vue'),
      meta: { requiresAuth: true },
      children: [
        // 用户管理
        {
          path: '/user/list',
          name: 'userList',
          component: () => import('../views/user/UserList.vue'),
          meta: { requiresAuth: true }
        },
        // 农产品管理
        {
          path: '/produce/list',
          name: 'produceList',
          component: () => import('../views/produce/ProduceList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/produce/add',
          name: 'produceAdd',
          component: () => import('../views/produce/ProduceAdd.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/produce/edit/:id',
          name: 'produceEdit',
          component: () => import('../views/produce/ProduceEdit.vue'),
          meta: { requiresAuth: true }
        },
        // 分类管理
        {
          path: '/category/list',
          name: 'categoryList',
          component: () => import('../views/category/CategoryList.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/category/add',
          name: 'categoryAdd',
          component: () => import('../views/category/CategoryAdd.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: '/category/edit/:id',
          name: 'categoryEdit',
          component: () => import('../views/category/CategoryEdit.vue'),
          meta: { requiresAuth: true }
        },
        // 订单管理
        {
          path: '/order/list',
          name: 'orderList',
          component: () => import('../views/order/OrderList.vue'),
          meta: { requiresAuth: true }
        },
        // 优惠券管理
        {
          path: '/coupon/list',
          name: 'couponList',
          component: () => import('../views/coupon/CouponList.vue'),
          meta: { requiresAuth: true }
        },
        // 套餐管理
        {
          path: '/package/list',
          name: 'packageList',
          component: () => import('../views/package/PackageList.vue'),
          meta: { requiresAuth: true }
        }
      ]
    }
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const tokenStore = useTokenStore()
  const token = tokenStore.token
  
  if (to.meta.requiresAuth === false) {
    // 不需要认证的页面，直接放行
    next()
  } else {
    // 需要认证的页面
    if (token) {
      // 有token，放行
      next()
    } else {
      // 没有token，跳转到登录页
      next({ path: '/login' })
    }
  }
})

export default router
