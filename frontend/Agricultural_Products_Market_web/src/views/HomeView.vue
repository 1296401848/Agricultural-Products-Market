<template>
  <div class="home-container">
    <!-- 头部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <!-- 移动端汉堡菜单按钮 -->
          <el-button class="hamburger-btn" @click="mobileMenuVisible = true" text>
            <el-icon :size="24"><Menu /></el-icon>
          </el-button>
          <div class="logo">农产品商城</div>
        </div>

        <!-- 桌面端导航栏 -->
        <nav class="nav-bar">
          <el-menu :default-active="activeNav" mode="horizontal" background-color="#fff" text-color="#606266" active-text-color="#409eff">
            <el-menu-item index="home" @click="goToHome">
              <template #icon>
                <el-icon><House /></el-icon>
              </template>
              首页
            </el-menu-item>
            <el-menu-item index="cart" @click="goToCart">
              <template #icon>
                <el-icon><ShoppingCart /></el-icon>
              </template>
              购物车
            </el-menu-item>
            <el-menu-item index="profile" @click="goToProfile">
              <template #icon>
                <el-icon><User /></el-icon>
              </template>
              个人中心
            </el-menu-item>
          </el-menu>
        </nav>

        <div class="user-actions">
          <el-button v-if="!token" @click="goToLogin">登录</el-button>
          <el-button v-else @click="handleLogout" type="danger">退出登录</el-button>
          <el-button v-if="!token" @click="goToRegister" type="primary">注册</el-button>
        </div>
      </div>
    </header>

    <!-- 移动端侧边导航抽屉 -->
    <el-drawer
      v-model="mobileMenuVisible"
      direction="ltr"
      size="70%"
      :with-header="false"
    >
      <el-menu
        :default-active="activeNav"
        mode="vertical"
        background-color="#fff"
        text-color="#606266"
        active-text-color="#409eff"
        @select="handleMobileMenuSelect"
      >
        <el-menu-item index="home">
          <template #icon>
            <el-icon><House /></el-icon>
          </template>
          首页
        </el-menu-item>
        <el-menu-item index="cart">
          <template #icon>
            <el-icon><ShoppingCart /></el-icon>
          </template>
          购物车
        </el-menu-item>
        <el-menu-item index="profile">
          <template #icon>
            <el-icon><User /></el-icon>
          </template>
          个人中心
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <!-- 主要内容 -->
    <main class="main-content">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator=">" class="breadcrumb" v-if="breadcrumbs.length > 0">
        <el-breadcrumb-item v-for="(item, i) in breadcrumbs" :key="i" :to="i < breadcrumbs.length - 1 ? item.to : undefined">
          {{ item.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
      <!-- 路由出口 -->
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-content">
        <div class="footer-left">
          <span class="footer-logo">农产品商城</span>
          <span class="footer-copy">© 2026 Produceshop. All rights reserved.</span>
        </div>
        <div class="footer-right">
          <span>联系邮箱：1296401848@qq.com</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { useUserStore } from '@/stores/user.js'
import { categoryCache } from '@/stores/category.js'
import { House, ShoppingCart, Document, User, Menu, Present, Goods } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const userStore = useUserStore()
const token = tokenStore.token
const user = userStore.userInfo
const activeNav = ref('home')
const mobileMenuVisible = ref(false)

// 面包屑
const breadNameMap = {
  '': '首页',
  'produceList': '农产品列表',
  'produceDetail': '农产品详情',
  'cart': '购物车',
  'orders': '我的订单',
  'checkout': '确认订单',
  'profile': '个人中心',
  'couponCenter': '领券中心',
  'packageList': '套餐专区',
  'packageDetail': '套餐详情',
  'search': '搜索',
}
const breadcrumbs = computed(() => {
  const crumbs = [{ name: '首页', to: '/' }]
  for (const r of route.matched) {
    if (!r.name || r.name === 'home') continue
    const name = breadNameMap[r.name] || r.name
    if (!crumbs.some(c => c.name === name)) {
      crumbs.push({ name, to: route.path })
    }
  }
  // 农产品详情：中间插入分类
  if (route.name === 'produceDetail' && categoryCache.categoryName) {
    crumbs.splice(1, 0, { name: categoryCache.categoryName, to: '/search?categoryId=' + categoryCache.categoryId })
  }
  // 套餐详情：中间插入「套餐专区」
  if (route.name === 'packageDetail') {
    crumbs.splice(1, 0, { name: '套餐专区', to: '/package' })
  }
  // 个人中心子页签
  if (route.name === 'profile' && route.query.tab) {
    const tabMap = { info: '个人信息', orders: '订单管理', coupons: '领券中心', addresses: '地址管理' }
    crumbs.push({ name: tabMap[route.query.tab] || route.query.tab, to: route.fullPath })
  }
  return crumbs.length > 1 ? crumbs : []
})

// 更新导航高亮状态
const updateActiveNav = (path) => {
  if (path === '/') {
    activeNav.value = 'home'
  } else if (path.startsWith('/cart')) {
    activeNav.value = 'cart'
  } else if (path.startsWith('/order')) {
    activeNav.value = 'orders'
  } else if (path.startsWith('/profile')) {
    activeNav.value = 'profile'
  } else if (path.startsWith('/coupon-center')) {
    activeNav.value = 'couponCenter'
  } else if (path.startsWith('/package')) {
    activeNav.value = 'packageList'
  }
}

// 路由变化时更新导航高亮
watch(
  () => route.path,
  (newPath) => {
    updateActiveNav(newPath)
  },
  { immediate: true }
)

// 初始化时更新导航高亮
onMounted(() => {
  updateActiveNav(route.path)
})

// 跳转到首页
const goToHome = () => {
  activeNav.value = 'home'
  router.push('/')
}

// 跳转到登录
const goToLogin = () => {
  router.push('/login')
}

// 跳转到注册
const goToRegister = () => {
  router.push('/register')
}

// 跳转到购物车
const goToCart = () => {
  activeNav.value = 'cart'
  router.push('/cart')
}

// 跳转到订单
const goToOrders = () => {
  activeNav.value = 'orders'
  router.push('/orders')
}

// 跳转到个人中心
const goToProfile = () => {
  activeNav.value = 'profile'
  router.push('/profile')
}

// 跳转到领券中心
const goToCouponCenter = () => {
  activeNav.value = 'couponCenter'
  router.push('/coupon-center')
}

// 跳转到套餐专区
const goToPackageList = () => {
  activeNav.value = 'packageList'
  router.push('/package')
}

// 退出登录
const handleLogout = () => {
  tokenStore.removeToken()
  userStore.removeUserInfo()
  activeNav.value = 'home'
  ElMessage.success('已退出登录')
  location.reload();
}

// 移动端菜单选择
const handleMobileMenuSelect = (index) => {
  mobileMenuVisible.value = false
  switch (index) {
    case 'home':
      goToHome()
      break
    case 'cart':
      goToCart()
      break
    case 'orders':
      goToOrders()
      break
    case 'profile':
      goToProfile()
      break
    case 'couponCenter':
      goToCouponCenter()
      break
    case 'packageList':
      goToPackageList()
      break
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.hamburger-btn {
  display: none;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #409eff;
  margin-right: 2rem;
}

.nav-bar {
  flex: 1;
}

.user-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: 2rem;
}

.breadcrumb {
  margin-bottom: 1rem;
  padding: 0.5rem 0;
}

.site-footer {
  background: #303133;
  color: #c0c4cc;
  padding: 1.25rem 0;
  margin-top: 2rem;
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.9rem;
}
.footer-left { display: flex; align-items: center; gap: 1.5rem; }
.footer-logo { color: #fff; font-weight: bold; }

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
  min-height: calc(100vh - 200px);
}

.produce-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.produce-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.produce-card:hover {
  transform: translateY(-5px);
}

.produce-cover {
  height: 200px;
  margin-bottom: 1rem;
}

.produce-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.produce-info {
  margin-bottom: 1rem;
}

.produce-title {
  font-weight: bold;
  font-size: 1.1rem;
}

.produce-manufacturer {
  color: #606266;
  margin-bottom: 0.5rem;
}

.produce-price {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.produce-stock {
  color: #67c23a;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
    padding: 0.5rem 0.75rem;
    gap: 0.5rem;
  }

  .hamburger-btn {
    display: inline-flex;
  }

  .logo {
    font-size: 1.2rem;
    margin-right: 0;
  }

  .nav-bar {
    display: none;
  }

  .user-actions {
    margin-left: 0;
  }

  .user-actions .el-button {
    padding: 0.4rem 0.8rem;
    font-size: 0.85rem;
  }

  .breadcrumb {
    padding: 0.25rem 0;
    font-size: 0.85rem;
  }

  .main-content {
    padding: 1rem 0.5rem;
  }

  .footer-content {
    flex-direction: column;
    text-align: center;
  }
  .footer-left {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>