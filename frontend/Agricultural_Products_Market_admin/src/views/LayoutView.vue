<template>
  <div class="admin-container">
    <!-- 桌面端侧边栏 -->
    <aside class="sidebar desktop-only">
      <div class="sidebar-header"><h3>农产品商城管理</h3></div>
      <el-menu default-active="1" class="el-menu-vertical-demo" :collapse="isCollapse" background-color="#001529" text-color="#fff" active-text-color="#409eff">
        <el-sub-menu index="1"><template #title><el-icon><User /></el-icon><span>用户管理</span></template><el-menu-item index="1-1" @click="navTo('/user/list')">用户列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="2"><template #title><el-icon><Document /></el-icon><span>农产品管理</span></template><el-menu-item index="2-1" @click="navTo('/produce/list')">农产品列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="3"><template #title><el-icon><Menu /></el-icon><span>分类管理</span></template><el-menu-item index="3-1" @click="navTo('/category/list')">分类列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="4"><template #title><el-icon><Tickets /></el-icon><span>订单管理</span></template><el-menu-item index="4-1" @click="navTo('/order/list')">订单列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="5"><template #title><el-icon><Present /></el-icon><span>优惠券管理</span></template><el-menu-item index="5-1" @click="navTo('/coupon/list')">优惠券列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="6"><template #title><el-icon><Goods /></el-icon><span>套餐管理</span></template><el-menu-item index="6-1" @click="navTo('/package/list')">套餐列表</el-menu-item></el-sub-menu>
      </el-menu>
    </aside>

    <!-- 主内容区域 -->
    <main class="main-content">
      <header class="top-header">
        <div class="header-left">
          <el-button class="hamburger-btn mobile-only" @click="mobileDrawer = true" text><el-icon :size="20"><Menu /></el-icon></el-button>
          <el-button class="desktop-only" text @click="toggleCollapse"><el-icon :size="20"><Fold /></el-icon></el-button>
        </div>
        <div class="header-right">
          <el-dropdown><span class="user-info"><el-icon><User /></el-icon><span class="desktop-only">{{ userStore.userInfo.username }}</span><el-icon class="el-icon--right"><ArrowDown /></el-icon></span>
            <template #dropdown><el-dropdown-menu><el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </header>
      <section class="content"><router-view /></section>
    </main>

    <!-- 移动端侧边抽屉 -->
    <el-drawer v-model="mobileDrawer" direction="ltr" size="220px" :with-header="false">
      <div class="mobile-nav-header"><h3>农产品商城管理</h3></div>
      <el-menu default-active="1" background-color="#fff" active-text-color="#409eff" @select="onMobileSelect">
        <el-sub-menu index="1"><template #title><el-icon><User /></el-icon><span>用户管理</span></template><el-menu-item index="/user/list">用户列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="2"><template #title><el-icon><Document /></el-icon><span>农产品管理</span></template><el-menu-item index="/produce/list">农产品列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="3"><template #title><el-icon><Menu /></el-icon><span>分类管理</span></template><el-menu-item index="/category/list">分类列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="4"><template #title><el-icon><Tickets /></el-icon><span>订单管理</span></template><el-menu-item index="/order/list">订单列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="5"><template #title><el-icon><Present /></el-icon><span>优惠券管理</span></template><el-menu-item index="/coupon/list">优惠券列表</el-menu-item></el-sub-menu>
        <el-sub-menu index="6"><template #title><el-icon><Goods /></el-icon><span>套餐管理</span></template><el-menu-item index="/package/list">套餐列表</el-menu-item></el-sub-menu>
      </el-menu>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import { useUserStore } from '@/stores/user.js'
import {
  User, Document, Menu, Tickets, ArrowDown, Fold, Present, Goods
} from '@element-plus/icons-vue'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()
const isCollapse = ref(false)
const mobileDrawer = ref(false)

const toggleCollapse = () => { isCollapse.value = !isCollapse.value }
const navTo = (path) => { router.push(path) }
const onMobileSelect = (index) => { mobileDrawer.value = false; router.push(index) }

const handleLogout = () => {
  tokenStore.removeToken()
  userStore.removeUserInfo()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.admin-container { display: flex; height: 100vh; overflow: hidden; }
.sidebar { width: 200px; background-color: #001529; color: #fff; transition: width 0.3s; overflow: hidden; flex-shrink: 0; }
.sidebar.collapsed { width: 64px; }
.sidebar-header { padding: 20px; text-align: center; border-bottom: 1px solid #1f2d3d; }
.sidebar-header h3 { margin: 0; font-size: 16px; color: #fff; }
.el-menu-vertical-demo { border-right: none; }
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.top-header { display: flex; justify-content: space-between; align-items: center; padding: 0 16px; height: 56px; background-color: #fff; border-bottom: 1px solid #e0e0e0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.header-left { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; cursor: pointer; padding: 6px 10px; border-radius: 4px; transition: background-color 0.3s; }
.user-info:hover { background-color: #f5f7fa; }
.user-info .el-icon { margin-right: 5px; }
.content { flex: 1; padding: 16px; background-color: #f5f7fa; overflow: auto; }
.mobile-nav-header { padding: 20px 16px 12px; border-bottom: 1px solid #ebeef5; }
.mobile-nav-header h3 { margin: 0; font-size: 15px; color: #303133; }

.desktop-only { display: block; }
.mobile-only { display: none; }

@media (max-width: 768px) {
  .desktop-only { display: none !important; }
  .mobile-only { display: block !important; }
  .sidebar.desktop-only { display: none; }
  .content { padding: 12px 8px; }
  .top-header { padding: 0 10px; }
}
</style>
