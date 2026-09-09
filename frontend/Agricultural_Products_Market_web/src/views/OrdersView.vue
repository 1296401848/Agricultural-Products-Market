<template>
  <div class="orders-container">

    <el-tabs v-model="orderTab">
      <el-tab-pane label="未支付" name="unpaid" />
      <el-tab-pane label="已支付" name="paid" />
      <el-tab-pane label="已取消" name="cancelled" />
    </el-tabs>

    <!-- 订单卡片列表 -->
    <div class="order-list" v-if="filteredOrders.length > 0">
      <el-card v-for="order in filteredOrders" :key="order.id" shadow="hover" class="order-card">
        <div class="order-card-header">
          <div class="order-primary">
            <span class="order-no">{{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.orderStatus)" size="small">
              {{ order.orderStatus || '未知状态' }}
            </el-tag>
            <el-tag :type="order.payStatus === 1 ? 'success' : 'warning'" size="small">
              {{ order.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </div>
          <div class="order-amount">¥{{ (order.payAmount || order.totalAmount).toFixed(2) }}</div>
        </div>

        <div class="order-card-body">
          <div class="order-meta">
            <span class="meta-item">
              <span class="meta-label">创建时间</span>
              <span class="meta-value">{{ order.createTime }}</span>
            </span>
            <span class="meta-item" v-if="order.payStatus === 0" style="color:#f56c6c;font-weight:bold">⏱ 剩余 {{ getCountdown(order) }}</span>
            <span class="meta-item" v-if="order.payTime">
              <span class="meta-label">支付时间</span>
              <span class="meta-value">{{ order.payTime }}</span>
            </span>
          </div>
        </div>

        <div class="order-card-actions">
          <el-button
            v-if="order.payStatus === 0 && order.orderStatus !== '已取消'"
            @click="handlePay(order.id)"
            type="primary"
            size="small"
            :loading="payLoading[order.id]"
          >
            立即支付
          </el-button>
          <el-button
            v-if="order.payStatus === 0 && order.orderStatus !== '已取消'"
            @click="handleCancel(order.id)"
            type="warning"
            size="small"
            :loading="cancelLoading[order.id]"
          >
            取消订单
          </el-button>
          <el-button v-if="order.payStatus === 1 && order.orderStatus === '已发货'" @click="handleConfirm(order.id)" type="primary" size="small">确认收货</el-button>
          <el-button @click="viewOrderDetail(order.id)" type="success" size="small">查看详情</el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="暂无订单" :image-size="200">
      <el-button type="primary" @click="goToHome">去购物</el-button>
    </el-empty>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="90%"
      class="order-detail-dialog-wrapper"
      append-to-body
    >
      <div v-if="orderDetail" class="order-detail-dialog">
        <!-- 订单基本信息 -->
        <div class="dialog-section">
          <h4>订单基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单编号：</span>
              <span class="value">{{ orderDetail.orderInfo.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态：</span>
              <el-tag :type="getStatusType(orderDetail.orderInfo.orderStatus)">{{ orderDetail.orderInfo.orderStatus || '未知状态' }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">支付状态：</span>
              <el-tag :type="orderDetail.orderInfo.payStatus === 1 ? 'success' : 'warning'">
                {{ orderDetail.orderInfo.payStatus === 1 ? '已支付' : '未支付' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">订单金额：</span>
              <span class="value price">¥{{ originalOrderTotal.toFixed(2) }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.orderInfo.couponDiscount > 0">
              <span class="label">优惠券抵扣：</span>
              <span class="value" style="color: #67c23a; font-weight: bold">-¥{{ orderDetail.orderInfo.couponDiscount?.toFixed(2) || '0.00' }}</span>
            </div>
            <div class="info-item">
              <span class="label">实付金额：</span>
              <span class="value price">¥{{ actualPayAmount.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ orderDetail.orderInfo.createTime }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.orderInfo.payTime">
              <span class="label">支付时间：</span>
              <span class="value">{{ orderDetail.orderInfo.payTime }}</span>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="dialog-section" v-if="orderDetail.recipient">
          <h4>收货信息</h4>
          <div class="address-info">
            <div class="info-item">
              <span class="label">收件人：</span>
              <span class="value">{{ orderDetail.recipient }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话：</span>
              <span class="value">{{ orderDetail.phone }}</span>
            </div>
            <div class="info-item full-width">
              <span class="label">收货地址：</span>
              <span class="value">{{ orderDetail.address }}</span>
            </div>
          </div>
        </div>

        <!-- 订单商品列表 -->
        <div class="dialog-section" v-if="orderDetail.orderItems && orderDetail.orderItems.length > 0">
          <h4>订单商品</h4>
          <div class="order-items-grouped">
            <div v-for="(group, gi) in groupedItems" :key="'g' + gi">
              <!-- 套餐头部 -->
              <div v-if="group.type === 'package'" class="pkg-group">
                <div class="pkg-group-header">
                  <el-icon><Present /></el-icon>
                  <span class="pkg-group-name">{{ group.packageName || '套餐 #' + group.packageId }}</span>
                  <el-tag type="warning" size="small">套餐</el-tag>
                </div>
                <div class="pkg-group-items">
                  <div v-for="(item, ii) in group.items" :key="ii" class="order-item-row">
                    <span class="item-name indent">{{ item.produceName }}</span>
                    <span class="item-price">¥{{ item.price?.toFixed(2) }}</span>
                    <span class="item-qty">×{{ item.quantity }}</span>
                    <span class="item-subtotal">¥{{ (item.price * item.quantity)?.toFixed(2) || '0.00' }}</span>
                  </div>
                </div>
              </div>
              <!-- 普通农产品 -->
              <div v-else class="order-item-row single-produce">
                <span class="item-name">{{ group.produceName }}</span>
                <span class="item-price">¥{{ group.price?.toFixed(2) }}</span>
                <span class="item-qty">×{{ group.quantity }}</span>
                <span class="item-subtotal">¥{{ (group.price * group.quantity)?.toFixed(2) || '0.00' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单总计 -->
        <div class="dialog-footer">
          <div class="total-info">
            <span class="total-label">总计：</span>
            <span class="total-price">¥{{ actualPayAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <el-skeleton :rows="10" animated v-else />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import {getOrderList, payOrder, cancelOrder, confirmOrder, getOrderDetail} from '@/apis/order.js'
import { getPackageDetail } from '@/apis/package.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const token = tokenStore.token

const orderTab = ref(route.query.paid === '1' ? 'paid' : 'unpaid')
const orders = ref([])
const filteredOrders = computed(() => {
  if (orderTab.value === 'unpaid') return orders.value.filter(o => o.payStatus === 0 && o.orderStatus !== '已取消')
  if (orderTab.value === 'paid') return orders.value.filter(o => o.payStatus === 1 && o.orderStatus !== '已取消')
  return orders.value.filter(o => o.orderStatus === '已取消')
})
const payLoading = ref({})
const cancelLoading = ref({})
const detailDialogVisible = ref(false)
const orderDetail = ref(null)
const packageNames = ref({})

const originalOrderTotal = computed(() => {
  const o = orderDetail.value?.orderInfo
  if (!o) return 0
  return (o.totalAmount || 0) + (o.couponDiscount || 0)
})

const actualPayAmount = computed(() => {
  const o = orderDetail.value?.orderInfo
  if (!o) return 0
  return o.payAmount || o.totalAmount || 0
})

const groupedItems = computed(() => {
  const items = orderDetail.value?.orderItems || []
  const groups = []
  const pkgMap = new Map()

  for (const item of items) {
    if (item.itemType === 2 && item.packageId) {
      if (!pkgMap.has(item.packageId)) {
        const group = { type: 'package', packageId: item.packageId, packageName: packageNames.value[item.packageId] || '', items: [] }
        pkgMap.set(item.packageId, group)
        groups.push(group)
      }
      pkgMap.get(item.packageId).items.push(item)
    } else {
      groups.push({ type: 'produce', ...item })
    }
  }
  return groups
})

// 监听路由变化，当从支付页面返回时刷新订单列表
watch(
  () => route.fullPath,
  (newPath) => {
    // 当路径包含支付相关参数或从支付页面返回时，刷新订单列表
    if (newPath === '/orders') {
      fetchOrderList()
    }
  }
)

// 获取订单列表
const fetchOrderList = async () => {
  if (!token) {
    ElMessage.warning('请先登录')
    await router.push('/login')
    return
  }
  
  try {
    const response = await getOrderList()
    if (response.code === 200) {
      orders.value = response.data
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
  }
}

// 获取订单状态标签类型
const getStatusType = (status) => {
  console.log('获取状态类型:', status)
  if (!status) {
    return 'info'
  }
  switch (status) {
    case '待发货':
    case '待支付':
      return 'warning'
    case '已发货':
      return 'info'
    case '已完成':
      return 'success'
    case '已取消':
      return 'danger'
    default:
      return 'info'
  }
}

// 倒计时
const now = ref(Date.now())
const autoCancelled = new Set()
let countdownTimer = null
const getCountdown = (order) => {
  if (order.payStatus !== 0) return ''
  const deadline = new Date(order.createTime).getTime() + 30 * 60 * 1000
  const remain = deadline - now.value
  if (remain <= 0) {
    if (!autoCancelled.has(order.id)) {
      autoCancelled.add(order.id)
      handleCancel(order.id)
    }
    return '已超时'
  }
  const m = Math.floor(remain / 60000)
  const s = Math.floor((remain % 60000) / 1000)
  return `${m}:${String(s).padStart(2, '0')}`
}
onMounted(() => { countdownTimer = setInterval(() => { now.value = Date.now() }, 1000) })
onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer) })

// 取消订单
const handleCancel = async (orderId) => {
  try {
    cancelLoading.value[orderId] = true
    await cancelOrder(orderId)
    ElMessage.success('订单已取消')
    fetchOrderList()
  } catch (error) {
    console.error('取消订单失败:', error)
  } finally {
    cancelLoading.value[orderId] = false
  }
}

const handleConfirm = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到货物？', '确认收货', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' })
    await confirmOrder(orderId)
    ElMessage.success('已确认收货')
    fetchOrderList()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.response?.data?.msg || '操作失败')
  }
}

// 处理支付
const handlePay = async (orderId) => {
  try {
    payLoading.value[orderId] = true
    const response = await payOrder({
      orderId
    })
    
    if (response.code === 200 && response.data) {
      // 创建一个隐藏的div用于显示支付表单
      const payDiv = document.createElement('div')
      payDiv.id = 'alipay-pay-form'
      payDiv.style.display = 'none'
      payDiv.innerHTML = response.data
      document.body.appendChild(payDiv)
      
      // 自动提交表单
      const form = payDiv.querySelector('form')
      if (form) {
        form.submit()
      } else {
        throw new Error('支付表单格式错误')
      }
    } else {
      throw new Error('获取支付表单失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败: ' + error.message)
  } finally {
    payLoading.value[orderId] = false
  }
}

// 查看订单详情
const viewOrderDetail = async (orderId) => {
  try {
    const response = await getOrderDetail(orderId)
    if (response.code === 200) {
      orderDetail.value = response.data
      // 获取套餐名称
      const pkgIds = [...new Set((response.data.orderItems || []).filter(i => i.packageId).map(i => i.packageId))]
      for (const pid of pkgIds) {
        try {
          const pr = await getPackageDetail(pid)
          if (pr.code === 200) packageNames.value[pid] = pr.data.packageInfo?.packageName || ''
        } catch (e) { /* ignore */ }
      }
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 跳转到首页
const goToHome = () => {
  router.push('/')
}

// 初始化
onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.orders-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.page-title {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: #303133;
  text-align: center;
}

/* ========== 订单卡片列表 ========== */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-card {
  transition: transform 0.3s;
}

.order-card:hover {
  transform: translateY(-3px);
}

/* 卡片头部：订单号 + 状态标签 + 金额 */
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #ebeef5;
}

.order-primary {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.order-no {
  font-weight: bold;
  color: #303133;
  font-size: 0.95rem;
}

.order-amount {
  font-size: 1.3rem;
  font-weight: bold;
  color: #f56c6c;
  white-space: nowrap;
}

/* 卡片内容：时间等元信息 */
.order-card-body {
  margin-bottom: 0.75rem;
}

.order-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 2rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.meta-label {
  color: #909399;
  flex-shrink: 0;
}

.meta-value {
  color: #606266;
}

/* 卡片操作按钮 */
.order-card-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  padding-top: 0.75rem;
  border-top: 1px solid #ebeef5;
}

/* ========== 订单详情弹窗样式 ========== */
.order-detail-dialog {
  padding: 0.5rem;
}

.dialog-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #ebeef5;
}

.dialog-section h4 {
  margin: 0 0 1rem 0;
  color: #303133;
  font-size: 1.1rem;
  font-weight: bold;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.info-item .label {
  width: 100px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  word-break: break-word;
}

.info-item .value.price {
  color: #f56c6c;
  font-weight: bold;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.dialog-footer {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.total-info {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.total-label {
  font-weight: bold;
  color: #303133;
  font-size: 1.1rem;
}

.total-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.3rem;
}

.order-items-grouped {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.pkg-group {
  border: 1px solid #fae3c4;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.pkg-group-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #fdf6ec;
  font-weight: bold;
  color: #e6a23c;
}

.pkg-group-name {
  flex: 1;
}

.pkg-group-items {
  padding: 0.25rem 0;
}

.order-item-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.4rem 0.75rem;
  border-bottom: 1px solid #ebeef5;
}
.order-item-row:last-child { border-bottom: none; }
.order-item-row.single-produce {
  padding: 0.5rem 0.75rem;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 0.35rem;
}

.item-name { flex: 1; color: #303133; }
.item-name.indent { padding-left: 1.25rem; }
.item-price { width: 80px; color: #606266; text-align: right; }
.item-qty { width: 50px; color: #909399; text-align: center; }
.item-subtotal { width: 90px; color: #f56c6c; font-weight: bold; text-align: right; }

/* 骨架屏样式 */
:deep(.el-skeleton) {
  margin-bottom: 1rem;
}

.table-wrapper {
  overflow-x: auto;
}

/* 订单详情弹窗桌面端最大宽度 */
:deep(.order-detail-dialog-wrapper) {
  max-width: 800px;
}

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .orders-container {
    padding: 0 0.5rem;
    margin: 1rem auto;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .order-card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-amount {
    align-self: flex-end;
  }

  .order-meta {
    flex-direction: column;
    gap: 0.4rem;
  }

  .order-card-actions {
    justify-content: stretch;
  }

  .order-card-actions .el-button {
    flex: 1;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-wrap: wrap;
  }
}
</style>