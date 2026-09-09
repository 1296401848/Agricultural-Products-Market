<template>
  <div class="cart-container">

    <!-- 购物车卡片列表 -->
    <div class="cart-list" v-if="cartItems.length > 0">
      <el-card v-for="item in cartItems" :key="item.id" shadow="hover" class="cart-item-card" :class="{ selected: selectedIds.has(item.id) }" @click="goToItem(item)">
        <div class="cart-item-row">
          <el-checkbox v-model="checkedMap[item.id]" @change="onCheckChange" class="cart-checkbox" @click.stop />

          <div class="cart-item-main">
            <div class="cart-item-cover">
              <el-image :src="getItemCover(item)" :fit="'cover'" />
            </div>
            <div class="cart-item-detail">
              <div class="cart-item-name">
                <el-tag v-if="item.cartType === 2" size="small" type="warning" style="margin-right:0.5rem">套餐</el-tag>
                {{ getItemName(item) }}
              </div>
              <div class="cart-item-unit-price">{{ getItemPriceLabel(item) }}</div>
            </div>
          </div>

          <div class="cart-item-controls" @click.stop>
            <div class="cart-item-quantity">
              <span class="control-label">数量</span>
              <el-input-number v-model="item.quantity" :min="1" :max="getItemMaxQty(item)" @change="updateCartQuantityHandler(item)" size="small" />
            </div>
            <div class="cart-item-subtotal">
              <span class="control-label">小计</span>
              <span class="subtotal-price">¥{{ getItemSubtotal(item).toFixed(2) }}</span>
            </div>
            <el-button type="danger" @click="deleteCartItemHandler(item.id)" :icon="Delete" size="small" circle />
          </div>
        </div>
      </el-card>

      <!-- 底部操作栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox v-model="selectAll" :indeterminate="isIndeterminate" @change="onSelectAll">全选</el-checkbox>
          <el-button v-if="selectedIds.size > 0" @click="batchDelete" type="danger" size="small" plain>
            删除选中 ({{ selectedIds.size }})
          </el-button>
        </div>
        <div class="footer-right">
          <div class="total-price">
            已选 <span class="price-num">{{ selectedCount }}</span> 件，
            <template v-if="bestCoupon">
              原价 <span style="text-decoration:line-through;color:#c0c4cc;font-size:0.9rem">¥{{ selectedTotal.toFixed(2) }}</span>
              合计：<span class="price-num">¥{{ selectedFinalTotal.toFixed(2) }}</span>
            </template>
            <template v-else>
              合计：<span class="price-num">¥{{ selectedTotal.toFixed(2) }}</span>
            </template>
            <div v-if="bestCoupon" class="best-coupon">
              🎫 已用：{{ bestCoupon.couponName }}（省¥{{ bestCoupon.discount.toFixed(2) }}）
            </div>
          </div>
          <div class="cart-actions">
            <el-button @click="goToHome">继续购物</el-button>
            <el-button type="primary" @click="goToCheckout" :disabled="selectedIds.size === 0">去结算</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="购物车为空" :image-size="200">
      <el-button type="primary" @click="goToHome">去购物</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getCartList, updateCartQuantity, deleteCartItem } from '@/apis/cart.js'
import { getProduceDetail } from '@/apis/produce.js'
import { getPackageDetail } from '@/apis/package.js'
import { getUserCoupons } from '@/apis/coupon.js'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const tokenStore = useTokenStore()
const token = tokenStore.token

const cartItems = ref([])
const produces = ref({})
const packages = ref({})
const userCoupons = ref([])

// 最佳优惠券
const bestCoupon = computed(() => {
  const total = selectedTotal.value
  let best = null
  for (const c of userCoupons.value) {
    if (c.useStatus !== 0) continue
    let discount = 0
    if (c.couponType === 1 && total >= (c.fullMoney || 0)) discount = c.reduceMoney || 0
    else if (c.couponType === 2) discount = c.reduceMoney || 0
    else if (c.couponType === 3) discount = total - total * (c.discount || 1)
    if (discount > 0 && (!best || discount > best.discount)) {
      best = { ...c, discount }
    }
  }
  return best
})

// 勾选状态
const checkedMap = reactive({})
const selectedIds = computed(() => {
  const ids = new Set()
  for (const id of Object.keys(checkedMap)) {
    if (checkedMap[id]) ids.add(Number(id))
  }
  return ids
})
const selectedCount = computed(() => {
  return cartItems.value.filter(i => selectedIds.value.has(i.id)).reduce((s, i) => s + i.quantity, 0)
})
const selectedTotal = computed(() => {
  return cartItems.value.filter(i => selectedIds.value.has(i.id)).reduce((s, i) => s + getItemSubtotal(i), 0)
})
const selectedFinalTotal = computed(() => {
  const discount = bestCoupon.value?.discount || 0
  return Math.max(0, selectedTotal.value - discount)
})
const selectAll = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(i => selectedIds.value.has(i.id)),
  set: (val) => onSelectAll(val)
})
const isIndeterminate = computed(() => {
  const sel = selectedIds.value.size
  return sel > 0 && sel < cartItems.value.length
})

const onCheckChange = () => { /* reactive auto-tracks */ }
const onSelectAll = (val) => {
  cartItems.value.forEach(i => { checkedMap[i.id] = val })
}
const batchDelete = async () => {
  const ids = [...selectedIds.value]
  for (const id of ids) {
    try { await deleteCartItem(id) } catch (e) { /* continue */ }
  }
  ElMessage.success(`已删除 ${ids.length} 项`)
  fetchCartList()
}

// 获取购物车列表
const fetchCartList = async () => {
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const response = await getCartList()
    if (response.code === 200) {
      cartItems.value = response.data
      // 获取农产品/套餐详情
      await Promise.all(cartItems.value.map(item => {
        if (item.cartType === 2) return fetchPackageDetail(item.packageId)
        return fetchProduceDetail(item.produceId)
      }))
    }
  } catch (error) {
    console.error('获取购物车列表失败:', error)
  }
}

// 获取农产品详情
const fetchProduceDetail = async (produceId) => {
  try {
    const response = await getProduceDetail(produceId)
    if (response.code === 200) {
      produces.value[produceId] = response.data
    }
  } catch (error) {
    console.error('获取农产品详情失败:', error)
  }
}

// 获取套餐详情
const fetchPackageDetail = async (pkgId) => {
  try {
    const response = await getPackageDetail(pkgId)
    if (response.code === 200) {
      packages.value[pkgId] = response.data
    }
  } catch (error) {
    console.error('获取套餐详情失败:', error)
  }
}

// ---- 通用渲染辅助 ----
const getPkgInfo = (pkgId) => packages.value[pkgId]?.packageInfo || {}
const getItemCover = (item) => {
  if (item.cartType === 2) return getPkgInfo(item.packageId).coverImg || 'https://via.placeholder.com/80'
  return produces.value[item.produceId]?.coverUrl || 'https://via.placeholder.com/80'
}
const getItemName = (item) => {
  if (item.cartType === 2) return getPkgInfo(item.packageId).packageName || '套餐'
  return produces.value[item.produceId]?.produceName || '未知农产品'
}
const getItemPriceLabel = (item) => {
  if (item.cartType === 2) return '套餐价：¥' + (getPkgInfo(item.packageId).packagePrice || 0).toFixed(2)
  return '单价：¥' + (produces.value[item.produceId]?.price || 0).toFixed(2)
}
const getItemSubtotal = (item) => {
  if (item.cartType === 2) return (getPkgInfo(item.packageId).packagePrice || 0) * item.quantity
  return (produces.value[item.produceId]?.price || 0) * item.quantity
}

// 更新购物车数量
const updateCartQuantityHandler = async (item) => {
  try {
    const response = await updateCartQuantity({
      id: item.id,
      quantity: item.quantity
    })
    if (response.code === 200) {
      ElMessage.success('更新成功')
    }
  } catch (error) {
    console.error('更新购物车数量失败:', error)
    // 恢复原数量
    fetchCartList()
  }
}

// 删除购物车项
const deleteCartItemHandler = async (id) => {
  try {
    const response = await deleteCartItem(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchCartList()
    }
  } catch (error) {
    console.error('删除购物车项失败:', error)
  }
}

// 计算总价格
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + getItemSubtotal(item), 0)
})

// 获取农产品封面
const getProduceCover = (produceId) => {
  return produces.value[produceId]?.coverUrl || 'https://via.placeholder.com/80'
}

// 获取农产品名称
const getProduceName = (produceId) => {
  return produces.value[produceId]?.produceName || '未知农产品'
}

// 获取农产品价格
const getProducePrice = (produceId) => {
  return produces.value[produceId]?.price || 0
}

const getProduceStock = (produceId) => {
  return produces.value[produceId]?.stock ?? 99
}

const getItemMaxQty = (item) => {
  if (item.cartType === 2) {
    const detail = packages.value[item.packageId]
    if (!detail?.produces) return 99
    let min = Infinity
    for (const b of detail.produces) {
      const maxForProduce = Math.floor((b.stock || 0) / (b.produceNum || 1))
      if (maxForProduce < min) min = maxForProduce
    }
    return min === Infinity ? 99 : Math.max(1, min)
  }
  return getProduceStock(item.produceId)
}
const goToItem = (item) => {
  if (item.cartType === 2) router.push('/package/' + item.packageId)
  else router.push('/produce/' + item.produceId)
}

const goToHome = () => {
  router.push('/')
}

// 跳转到结算
const goToCheckout = () => {
  const ids = [...selectedIds.value].join(',')
  const query = ids ? { cartIds: ids } : {}
  if (bestCoupon.value) query.couponId = bestCoupon.value.id
  router.push({ path: '/checkout', query })
}

// 初始化
onMounted(async () => {
  fetchCartList()
  try { const r = await getUserCoupons(0); if (r.code === 200) userCoupons.value = r.data } catch (e) { /* */ }
})
</script>

<style scoped>
.cart-container {
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

/* ========== 购物车卡片列表 ========== */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item-card {
  transition: transform 0.3s; cursor: pointer;
}
.cart-item-card:hover { transform: translateY(-3px); }
.cart-item-card.selected { border-color: #3EB135; background: #ECF7EB; }

.cart-checkbox { flex-shrink: 0; }
.cart-checkbox :deep(.el-checkbox__inner) { width: 20px; height: 20px; }
.cart-checkbox :deep(.el-checkbox__inner::after) { left: 7px; top: 3px; width: 5px; height: 10px; }
.footer-left :deep(.el-checkbox__inner) { width: 20px; height: 20px; }
.footer-left :deep(.el-checkbox__inner::after) { left: 7px; top: 3px; width: 5px; height: 10px; }

/* 卡片内容：横向排列 */
.cart-item-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* 左侧：封面 + 名称 */
.cart-item-main {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
  min-width: 200px;
}

.cart-item-cover {
  width: 80px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.cart-item-cover .el-image {
  width: 100%;
  height: 100%;
}

.cart-item-detail {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.cart-item-name {
  font-weight: bold;
  color: #303133;
  font-size: 1rem;
}

.cart-item-unit-price {
  color: #909399;
  font-size: 0.9rem;
}

/* 右侧：数量 + 小计 + 删除 */
.cart-item-controls {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.cart-item-quantity {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-item-subtotal {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.control-label {
  color: #909399;
  font-size: 0.85rem;
}

.subtotal-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.1rem;
  white-space: nowrap;
}

/* 底部操作栏 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 0.5rem;
  padding: 1.25rem 1.5rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.footer-left { display: flex; align-items: center; gap: 0.75rem; }
.footer-right { display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }

.total-price { font-size: 1.1rem; color: #303133; }
.price-num { font-size: 1.3rem; font-weight: bold; color: #f56c6c; }

.best-coupon { font-size: 0.85rem; color: #e6a23c; margin-top: 0.25rem; }
.cart-actions { display: flex; gap: 0.75rem; }

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .cart-container {
    padding: 0 0.5rem;
    margin: 1rem auto;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .cart-item-row {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }

  .cart-item-main {
    min-width: auto;
  }

  .cart-item-controls {
    justify-content: space-between;
    gap: 0.75rem;
    padding-top: 0.75rem;
    border-top: 1px solid #ebeef5;
  }

  .cart-item-quantity,
  .cart-item-subtotal {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }

  .cart-footer {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
    text-align: center;
    padding: 1rem;
  }
  .footer-left, .footer-right {
    justify-content: center;
  }
  .footer-right {
    flex-direction: column;
    gap: 0.5rem;
  }

  .cart-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .cart-item-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .cart-item-quantity {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .cart-item-subtotal {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>