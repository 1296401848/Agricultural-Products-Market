<template>
  <div class="checkout-container">
    <h2 class="page-title">确认订单</h2>
    
    <div class="checkout-content">
      <!-- 地址选择 -->
      <section class="checkout-section">
        <h3 class="section-title">选择收货地址</h3>
        <div class="address-selector">
          <div v-for="address in addresses" :key="address.id" class="address-item" :class="{ 'active': selectedAddressId === address.id }" @click="selectAddress(address.id)">
            <div class="address-header">
              <div class="recipient-info">
                <span class="recipient-name">{{ address.recipient }}</span>
                <span class="recipient-phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
              </div>
            </div>
            <div class="address-content">
              {{ address.address }}
            </div>
          </div>
          <div class="add-address-btn" @click="showAddAddressDialog">
            <el-button type="primary" plain>添加新地址</el-button>
          </div>
        </div>
      </section>

      <!-- 商品清单 -->
      <section class="checkout-section">
        <h3 class="section-title">商品清单</h3>
        <div class="cart-items">
          <el-card v-for="item in cartItems" :key="item.id" class="cart-item-card" @click="goToItem(item)">
            <div class="cart-item-content">
              <div class="cart-item-info">
                <div class="produce-cover-small">
                  <el-image :src="getItemCover(item) || 'https://via.placeholder.com/80'" :fit="'cover'" />
                </div>
                <div class="produce-info-small">
                  <div class="produce-title-small">{{ getItemName(item) }}</div>
                  <div class="produce-price-small">¥{{ getItemPrice(item).toFixed(2) }}</div>
                </div>
              </div>
              <div class="cart-item-quantity">x{{ item.quantity }}</div>
              <div class="cart-item-subtotal">¥{{ (getItemPrice(item) * item.quantity).toFixed(2) }}</div>
            </div>
          </el-card>
        </div>
      </section>

      <!-- 选择优惠券 -->
      <section class="checkout-section" v-if="coupons.length > 0">
        <h3 class="section-title">优惠券</h3>
        <div class="coupon-select">
          <el-select v-model="selectedCouponId" placeholder="选择优惠券" clearable @change="onCouponChange" style="width: 100%">
            <el-option label="不使用优惠券" :value="null" />
            <el-option v-for="c in coupons" :key="c.id" :label="couponLabel(c)" :value="c.id" />
          </el-select>
          <div v-if="discountAmount > 0" class="discount-info">
            优惠：<span class="discount-price">-¥{{ discountAmount.toFixed(2) }}</span>
          </div>
        </div>
      </section>

      <!-- 订单总价 -->
      <section class="checkout-section">
        <h3 class="section-title">订单总计</h3>
        <div class="order-total">
          <div class="total-price">
            <div v-if="discountAmount > 0" style="text-decoration: line-through; color: #909399; font-size: 0.9rem">
              原价：¥{{ totalPrice.toFixed(2) }}
            </div>
            总计：<span class="price-highlight">¥{{ finalPrice.toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" @click="submitOrder" :loading="loading" :disabled="!selectedAddressId">
            提交订单
          </el-button>
        </div>
      </section>
    </div>

    <!-- 添加地址对话框 -->
    <el-dialog v-model="dialogVisible" title="添加新地址" width="500px">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收件人" prop="recipient">
          <el-input v-model="addressForm.recipient" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" :rows="3" placeholder="请输入详细地址" />
          <div class="form-item-extra">
            <el-button type="primary" size="small" @click="mapDialogVisible = true">地图选择</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" true-label="1" false-label="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddAddress" :loading="dialogLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 地图选择器对话框 -->
    <el-dialog v-model="mapDialogVisible" title="地图选择地址" width="900px">
      <MapSelector @confirm="handleMapConfirm" @cancel="mapDialogVisible = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getCartList } from '@/apis/cart.js'
import { getProduceDetail } from '@/apis/produce.js'
import { getAddressList, addAddress } from '@/apis/address.js'
import { createOrder } from '@/apis/order.js'
import { getUserCoupons } from '@/apis/coupon.js'
import { getPackageDetail } from '@/apis/package.js'
import { ElMessage } from 'element-plus'
import MapSelector from '@/components/MapSelector.vue'

const router = useRouter()
const route = useRoute()
const tokenStore = useTokenStore()
const token = tokenStore.token

// 购物车数据
const cartItems = ref([])
const produces = ref({})
const packages = ref({})

// 地址数据
const addresses = ref([])
const selectedAddressId = ref(null)

// 优惠券
const coupons = ref([])
const selectedCouponId = ref(null)
const discountAmount = ref(0)

const fetchCoupons = async () => {
  try {
    const res = await getUserCoupons(0)
    if (res.code === 200) {
      coupons.value = res.data
      // 自动选择最佳优惠券
      if (route.query.couponId) {
        const id = Number(route.query.couponId)
        if (coupons.value.some(c => c.id === id)) {
          selectedCouponId.value = id
          onCouponChange()
          return
        }
      }
      // 无指定券时，自动选最优
      autoSelectBest()
    }
  } catch (e) { /* ignore */ }
}

const autoSelectBest = () => {
  let bestId = null, bestDiscount = 0
  for (const c of coupons.value) {
    let d = 0
    if (c.couponType === 1 && totalPrice.value >= (c.fullMoney || 0)) d = c.reduceMoney || 0
    else if (c.couponType === 2) d = c.reduceMoney || 0
    else if (c.couponType === 3) d = totalPrice.value - totalPrice.value * (c.discount || 1)
    if (d > bestDiscount) { bestDiscount = d; bestId = c.id }
  }
  if (bestId) { selectedCouponId.value = bestId; onCouponChange() }
}

const couponLabel = (c) => {
  if (c.couponType === 3) return `${c.couponName}（打${(c.discount * 10).toFixed(0)}折）`
  if (c.couponType === 2) return `${c.couponName}（减¥${c.reduceMoney}）`
  return `${c.couponName}（满¥${c.fullMoney}减¥${c.reduceMoney}）`
}

const onCouponChange = () => {
  if (!selectedCouponId.value) { discountAmount.value = 0; return }
  const c = coupons.value.find(x => x.id === selectedCouponId.value)
  if (!c) { discountAmount.value = 0; return }
  if (c.couponType === 3) {
    discountAmount.value = totalPrice.value - totalPrice.value * c.discount
  } else if (c.couponType === 2) {
    discountAmount.value = Math.min(c.reduceMoney, totalPrice.value)
  } else if (c.couponType === 1) {
    discountAmount.value = totalPrice.value >= c.fullMoney ? c.reduceMoney : 0
  }
  if (discountAmount.value < 0) discountAmount.value = 0
}

const finalPrice = computed(() => {
  return Math.max(0, totalPrice.value - discountAmount.value)
})

// 对话框数据
const dialogVisible = ref(false)
const mapDialogVisible = ref(false)
const addressFormRef = ref()
const dialogLoading = ref(false)
const loading = ref(false)

const addressForm = ref({
  recipient: '',
  phone: '',
  address: '',
  isDefault: 0
})

const rules = {
  recipient: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
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
      // 如果从购物车勾选进入，只保留选中的项
      if (route.query.cartIds) {
        const ids = new Set(route.query.cartIds.split(',').map(Number))
        cartItems.value = cartItems.value.filter(item => ids.has(item.id))
      }
      // 获取农产品/套餐详情
      await Promise.all(cartItems.value.map(item => {
        if (item.cartType === 2) return fetchPackageDetail(item.packageId)
        return fetchProduceDetail(item.produceId)
      }))
    }
  } catch (error) {
    console.error('获取购物车列表失败:', error)
    ElMessage.error('获取购物车列表失败')
  }
}

// 获取农产品详情
const fetchProduceDetail = async (produceId) => {
  if (!produceId) return
  try {
    const response = await getProduceDetail(produceId)
    if (response.code === 200) produces.value[produceId] = response.data
  } catch (error) { console.error('获取农产品详情失败:', error) }
}

// 获取套餐详情
const fetchPackageDetail = async (pkgId) => {
  if (!pkgId) return
  try {
    const response = await getPackageDetail(pkgId)
    if (response.code === 200) packages.value[pkgId] = response.data.packageInfo
  } catch (error) { console.error('获取套餐详情失败:', error) }
}

const goToItem = (item) => {
  if (item.cartType === 2) router.push('/package/' + item.packageId)
  else router.push('/produce/' + item.produceId)
}

const getItemName = (item) => {
  if (item.cartType === 2) return packages.value[item.packageId]?.packageName || '套餐'
  return produces.value[item.produceId]?.produceName || '未知农产品'
}
const getItemCover = (item) => {
  if (item.cartType === 2) return packages.value[item.packageId]?.coverImg || ''
  return produces.value[item.produceId]?.coverUrl || ''
}
const getItemPrice = (item) => {
  if (item.cartType === 2) return packages.value[item.packageId]?.packagePrice || 0
  return produces.value[item.produceId]?.price || 0
}

// 获取地址列表
const fetchAddressList = async () => {
  try {
    const response = await getAddressList()
    if (response.code === 200) {
      addresses.value = response.data
      // 默认选择第一个地址或默认地址
      if (addresses.value.length > 0) {
        const defaultAddress = addresses.value.find(addr => addr.isDefault === 1)
        selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
      }
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 选择地址
const selectAddress = (id) => {
  selectedAddressId.value = id
}

// 显示添加地址对话框
const showAddAddressDialog = () => {
  addressForm.value = {
    recipient: '',
    phone: '',
    address: '',
    isDefault: 0
  }
  dialogVisible.value = true
}

// 处理地图选择结果
const handleMapConfirm = (addressData) => {
  addressForm.value.address = addressData.fullAddress
  mapDialogVisible.value = false
  ElMessage.success('地址选择成功')
}

// 处理添加地址
const handleAddAddress = async () => {
  if (!addressFormRef.value) return
  await addressFormRef.value.validate(async (valid) => {
    if (valid) {
      dialogLoading.value = true
      try {
        const response = await addAddress(addressForm.value)
        if (response.code === 200) {
          // 成功提示由拦截器统一处理
          dialogVisible.value = false
          await fetchAddressList()
        }
      } catch (error) {
        console.error('添加地址失败:', error)
        ElMessage.error('添加地址失败')
      } finally {
        dialogLoading.value = false
      }
    }
  })
}

// 计算总价格
const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    return sum + (getItemPrice(item) * item.quantity)
  }, 0)
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

// 提交订单
const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  loading.value = true
  try {
    const response = await createOrder({
      addressId: selectedAddressId.value,
      userCouponId: selectedCouponId.value || undefined
    })
    if (response.code === 200) {
      // 跳转到订单列表页面（成功提示由拦截器统一处理）
      router.push('/orders')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(async () => {
  await fetchCartList()
  await fetchAddressList()
  fetchCoupons()
})
</script>

<style scoped>
.checkout-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.page-title {
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #303133;
  text-align: center;
}

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.checkout-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #303133;
  border-bottom: 2px solid #3EB135;
  padding-bottom: 0.5rem;
}

/* 地址选择样式 */
.address-selector {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1rem;
}

.address-item {
  border: 2px solid #ebeef5;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
}

.address-item:hover {
  border-color: #3EB135;
  background-color: #ECF7EB;
}

.address-item.active {
  border-color: #3EB135;
  background-color: #ECF7EB;
  box-shadow: 0 0 0 2px rgba(62, 177, 53, 0.2);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.recipient-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.recipient-name {
  font-weight: bold;
  color: #303133;
}

.recipient-phone {
  color: #606266;
}

.address-content {
  color: #606266;
  line-height: 1.5;
}

.add-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 2rem;
  background-color: #fafafa;
}

/* 商品清单样式 */
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item-card {
  transition: all 0.3s;
  cursor: pointer;
}

.cart-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cart-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
}

.cart-item-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.produce-cover-small {
  width: 80px;
  height: 100px;
  flex-shrink: 0;
}

.produce-info-small {
  flex: 1;
}

.produce-title-small {
  font-weight: bold;
  color: #303133;
  margin-bottom: 0.5rem;
}

.produce-price-small {
  color: #f56c6c;
  font-weight: bold;
}

.cart-item-quantity {
  color: #606266;
  font-size: 1.1rem;
  width: 80px;
  text-align: center;
}

.cart-item-subtotal {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.1rem;
  width: 100px;
  text-align: right;
}

/* 订单总计样式 */
.order-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #fafafa;
  border-radius: 8px;
}

.total-price {
  font-size: 1.3rem;
  font-weight: bold;
  color: #303133;
}

.price-highlight {
  color: #f56c6c;
  font-size: 1.5rem;
}

.form-item-extra {
  margin-top: 0.5rem;
  display: flex;
  justify-content: flex-end;
}

.coupon-select { display: flex; flex-direction: column; gap: 0.5rem; }
.discount-info { font-size: 0.95rem; color: #606266; }
.discount-price { color: #67c23a; font-weight: bold; font-size: 1.1rem; }

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .checkout-container {
    padding: 1rem 0.5rem;
    margin: 1rem auto;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .address-selector {
    grid-template-columns: 1fr;
  }

  .cart-item-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .cart-item-subtotal {
    text-align: left;
    width: auto;
  }

  .order-total {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
    text-align: center;
  }

  .checkout-section {
    padding: 1rem;
  }
}
</style>