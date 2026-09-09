<template>
  <div class="profile-layout">
    <!-- 侧边栏 -->
    <div class="profile-sidebar">
      <el-menu :default-active="activeTab" @select="activeTab = $event" class="sidebar-menu">
        <el-menu-item index="info">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="orders">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="coupons">
          <el-icon><Present /></el-icon>
          <span>领券中心</span>
        </el-menu-item>
        <el-menu-item index="addresses">
          <el-icon><Location /></el-icon>
          <span>地址管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 内容区域 -->
    <div class="profile-main">

      <!-- 个人信息 -->
      <div v-show="activeTab === 'info'">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户信息</span>
              <el-button type="primary" size="small" @click="showPasswordDialog">修改密码</el-button>
            </div>
          </template>
          <div class="user-info">
            <div class="info-item"><span class="label">用户名：</span><span class="value">{{ userInfo.username || '未设置' }}</span></div>
            <div class="info-item"><span class="label">邮箱：</span><span class="value">{{ userInfo.email || '未设置' }}</span></div>
            <div class="info-item"><span class="label">身份：</span><span class="value">{{ roleName(userInfo.role) }}</span></div>
            <div class="info-item"><span class="label">注册时间：</span><span class="value">{{ userInfo.createdTime || '未设置' }}</span></div>
          </div>
        </el-card>

        <div class="contact-us">
          <el-button type="primary" link @click="contactVisible = true">联系我们</el-button>
        </div>
      </div>

      <!-- 订单管理 -->
      <div v-show="activeTab === 'orders'">
        <el-tabs v-model="orderTab" @tab-change="fetchOrderList">
          <el-tab-pane label="未支付" name="unpaid" />
          <el-tab-pane label="已支付" name="paid" />
          <el-tab-pane label="已取消" name="cancelled" />
        </el-tabs>
        <div v-if="filteredOrders.length > 0">
          <div class="order-list">
            <el-card v-for="order in filteredOrders" :key="order.id" shadow="hover" class="order-card">
              <div class="order-card-header">
                <div class="order-primary">
                  <span class="order-no">{{ order.orderNo }}</span>
                  <el-tag :type="getStatusType(order.orderStatus)" size="small">{{ order.orderStatus || '未知' }}</el-tag>
                  <el-tag :type="order.payStatus === 1 ? 'success' : 'warning'" size="small">{{ order.payStatus === 1 ? '已支付' : '未支付' }}</el-tag>
                </div>
                <div class="order-amount">¥{{ (order.payAmount || order.totalAmount).toFixed(2) }}</div>
              </div>
              <div class="order-card-body">
                <div class="order-meta">
                  <span class="meta-item"><span class="meta-label">创建时间</span><span class="meta-value">{{ order.createTime }}</span></span>
                  <span class="meta-item" v-if="order.payStatus === 0" style="color:#f56c6c;font-weight:bold">⏱ 剩余 {{ getCountdown(order) }}</span>
                  <span class="meta-item" v-if="order.payTime"><span class="meta-label">支付时间</span><span class="meta-value">{{ order.payTime }}</span></span>
                </div>
              </div>
              <div class="order-card-actions">
                <el-button v-if="order.payStatus === 0 && order.orderStatus !== '已取消'" @click="handlePay(order.id)" type="primary" size="small" :loading="payLoading[order.id]">立即支付</el-button>
                <el-button v-if="order.payStatus === 0 && order.orderStatus !== '已取消'" @click="handleCancel(order.id)" type="warning" size="small" :loading="cancelLoading[order.id]">取消订单</el-button>
                <el-button v-if="order.payStatus === 1 && order.orderStatus === '已发货'" @click="handleConfirm(order.id)" type="primary" size="small">确认收货</el-button>
                <el-button @click="viewOrderDetail(order.id)" type="success" size="small">查看详情</el-button>
              </div>
            </el-card>
          </div>
        </div>
        <el-empty v-else description="暂无订单" :image-size="150">
          <el-button type="primary" @click="$router.push('/')">去购物</el-button>
        </el-empty>
      </div>

      <!-- 领券中心 -->
      <div v-show="activeTab === 'coupons'">
        <el-tabs v-model="couponTab">
          <el-tab-pane label="可领优惠券" name="available" />
          <el-tab-pane label="我的优惠券" name="mine" />
        </el-tabs>
        <div v-if="couponTab === 'available'" class="coupon-grid">
          <div v-for="c in availableList" :key="c.id" class="coupon-card" :class="{ disabled: c.usedCount >= c.totalCount }">
            <div class="coupon-left">
              <div class="coupon-type-tag">{{ typeMap[c.couponType] }}</div>
              <div class="coupon-value">
                <template v-if="c.couponType === 3"><span class="num">{{ (c.discount * 10).toFixed(0) }}</span><span class="unit">折</span></template>
                <template v-else><span class="symbol">¥</span><span class="num">{{ c.reduceMoney }}</span></template>
              </div>
              <div class="coupon-condition" v-if="c.couponType === 1">满{{ c.fullMoney }}</div>
              <div class="coupon-condition" v-else-if="c.couponType === 2">无门槛</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.couponName }}</div>
              <div class="coupon-time">领取后{{ c.validDays }}天有效</div>
              <div class="coupon-count">已领 {{ c.usedCount }}/{{ c.totalCount }}</div>
              <el-button type="primary" size="small" @click="doReceive(c.id)" :loading="receiving[c.id]" :disabled="isClaimed(c.id) || c.usedCount >= c.totalCount">
                {{ isClaimed(c.id) ? '已领取' : c.usedCount >= c.totalCount ? '已抢光' : '立即领取' }}
              </el-button>
            </div>
          </div>
          <el-empty v-if="availableList.length === 0" description="暂无可领优惠券" :image-size="120" />
        </div>
        <div v-if="couponTab === 'mine'" class="coupon-grid">
          <div v-for="c in myList" :key="c.id" class="coupon-card" :class="{ used: c.useStatus !== 0 }">
            <div class="coupon-left"><div class="coupon-type-tag">{{ typeMap[c.couponType] }}</div><div class="coupon-value"><span class="symbol">¥</span><span class="num">{{ c.reduceMoney }}</span></div></div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.couponName }}</div>
              <div class="coupon-time">{{ c.validStart }} ~ {{ c.validEnd }}</div>
              <el-tag :type="c.useStatus === 0 ? 'primary' : c.useStatus === 1 ? 'info' : 'warning'" size="small">{{ statusMap[c.useStatus] }}</el-tag>
            </div>
          </div>
          <el-empty v-if="myList.length === 0" description="暂无优惠券" :image-size="120" />
        </div>
      </div>

      <!-- 地址管理 -->
      <div v-show="activeTab === 'addresses'">
        <div v-if="addresses.length > 0" class="address-section">
          <div class="address-header-bar">
            <span></span>
            <el-button type="primary" size="small" @click="showAddDialog">添加新地址</el-button>
          </div>
          <div class="address-list">
          <el-card v-for="address in addresses" :key="address.id" shadow="hover" class="address-item">
            <div class="address-header">
              <div class="recipient-info">
                <span class="recipient-name">{{ address.recipient }}</span>
                <span class="recipient-phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault === 1" type="success" size="small">默认</el-tag>
              </div>
              <div class="address-operations">
                <el-button v-if="address.isDefault === 0" @click="setDefaultAddress(address.id)" type="primary" size="small">设为默认</el-button>
                <el-button @click="showEditDialog(address)" type="success" size="small">编辑</el-button>
                <el-button @click="deleteAddressHandler(address.id)" type="danger" size="small">删除</el-button>
              </div>
            </div>
            <div class="address-content">{{ address.address }}</div>
          </el-card>
        </div>
        </div>
        <el-empty v-else description="暂无地址" :image-size="120">
          <el-button type="primary" @click="showAddDialog">添加地址</el-button>
        </el-empty>
      </div>

    </div>

    <!-- 弹窗：地址编辑、修改密码、地图、联系我们、订单详情 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" class="address-dialog">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef" label-width="80px">
        <el-form-item label="收件人" prop="recipient"><el-input v-model="addressForm.recipient" placeholder="请输入收件人姓名" /></el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model="addressForm.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" :rows="3" placeholder="请输入详细地址" />
          <div class="form-item-extra"><el-button type="primary" size="small" @click="mapDialogVisible = true">地图选择</el-button></div>
        </el-form-item>
        <el-form-item><el-checkbox v-model="addressForm.isDefault" true-label="1" false-label="0">设为默认地址</el-checkbox></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="90%" class="password-dialog">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword"><el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password /></el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword"><el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="passwordDialogVisible = false">取消</el-button><el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="mapDialogVisible" title="地图选择地址" width="95%" class="map-dialog">
      <MapSelector @confirm="handleMapConfirm" @cancel="mapDialogVisible = false" />
    </el-dialog>

    <el-dialog v-model="contactVisible" title="联系我们" width="400px" append-to-body>
      <div class="contact-body"><p class="contact-text">如有疑问或账号问题，请通过以下邮箱联系我们：</p><div class="contact-email">1296401848@qq.com</div></div>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="90%" class="order-detail-dialog-wrapper" append-to-body>
      <div v-if="orderDetail" class="order-detail-dialog">
        <div class="dialog-section"><h4>订单基本信息</h4>
          <div class="info-grid">
            <div class="info-item"><span class="label">订单编号：</span><span class="value">{{ orderDetail.orderInfo.orderNo }}</span></div>
            <div class="info-item"><span class="label">订单状态：</span><el-tag :type="getStatusType(orderDetail.orderInfo.orderStatus)">{{ orderDetail.orderInfo.orderStatus || '未知' }}</el-tag></div>
            <div class="info-item"><span class="label">支付状态：</span><el-tag :type="orderDetail.orderInfo.payStatus === 1 ? 'success' : 'warning'">{{ orderDetail.orderInfo.payStatus === 1 ? '已支付' : '未支付' }}</el-tag></div>
            <div class="info-item"><span class="label">订单金额：</span><span class="value price">¥{{ detailOriginalTotal.toFixed(2) }}</span></div>
            <div class="info-item" v-if="orderDetail.orderInfo.couponDiscount > 0"><span class="label">优惠券抵扣：</span><span class="value" style="color:#3EB135;font-weight:bold">-¥{{ orderDetail.orderInfo.couponDiscount.toFixed(2) }}</span></div>
            <div class="info-item"><span class="label">实付金额：</span><span class="value price">¥{{ detailPayAmount.toFixed(2) }}</span></div>
            <div class="info-item"><span class="label">创建时间：</span><span class="value">{{ orderDetail.orderInfo.createTime }}</span></div>
            <div class="info-item" v-if="orderDetail.orderInfo.payTime"><span class="label">支付时间：</span><span class="value">{{ orderDetail.orderInfo.payTime }}</span></div>
          </div>
        </div>
        <div class="dialog-section" v-if="orderDetail.recipient"><h4>收货信息</h4>
          <div class="address-info">
            <div class="info-item"><span class="label">收件人：</span><span class="value">{{ orderDetail.recipient }}</span></div>
            <div class="info-item"><span class="label">联系电话：</span><span class="value">{{ orderDetail.phone }}</span></div>
            <div class="info-item"><span class="label">收货地址：</span><span class="value">{{ orderDetail.address }}</span></div>
          </div>
        </div>
        <div class="dialog-section" v-if="orderDetail.orderItems?.length"><h4>订单商品</h4>
          <div class="order-items-grouped">
            <div v-for="(group, gi) in groupedItems" :key="'g'+gi">
              <div v-if="group.type === 'package'" class="pkg-group">
                <div class="pkg-group-header"><el-icon><Present /></el-icon><span class="pkg-group-name">{{ group.packageName || '套餐 #'+group.packageId }}</span><el-tag type="warning" size="small">套餐</el-tag></div>
                <div class="pkg-group-items">
                  <div v-for="(item, ii) in group.items" :key="ii" class="order-item-row">
                    <span class="item-name indent">{{ item.produceName }}</span><span class="item-price">¥{{ item.price?.toFixed(2) }}</span><span class="item-qty">×{{ item.quantity }}</span><span class="item-subtotal">¥{{ (item.price*item.quantity)?.toFixed(2) || '0.00' }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="order-item-row single-produce">
                <span class="item-name">{{ group.produceName }}</span><span class="item-price">¥{{ group.price?.toFixed(2) }}</span><span class="item-qty">×{{ group.quantity }}</span><span class="item-subtotal">¥{{ (group.price*group.quantity)?.toFixed(2) || '0.00' }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="dialog-footer"><div class="total-info"><span class="total-label">总计：</span><span class="total-price">¥{{ detailPayAmount.toFixed(2) }}</span></div></div>
      </div>
      <el-skeleton :rows="10" animated v-else />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { useUserStore } from '@/stores/user.js'
import { getAddressList, addAddress, updateAddress, deleteAddress } from '@/apis/address.js'
import { updatePassword } from '@/apis/user.js'
import { getOrderList, payOrder, cancelOrder, confirmOrder, getOrderDetail } from '@/apis/order.js'
import { getAvailableCoupons, receiveCoupon, getUserCoupons } from '@/apis/coupon.js'
import { getPackageDetail } from '@/apis/package.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Present, Location } from '@element-plus/icons-vue'
import MapSelector from '@/components/MapSelector.vue'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()

const activeTab = ref(route.query.tab || 'info')

// 进入页面时如果没有 tab 参数，默认补上
if (!route.query.tab) {
  router.replace({ query: { tab: activeTab.value } })
}

watch(activeTab, (val) => {
  router.replace({ query: { tab: val } })
})
const userInfo = ref({})
const contactVisible = ref(false)

// ==== 订单 ====
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

const detailOriginalTotal = computed(() => {
  const o = orderDetail.value?.orderInfo; if (!o) return 0; return (o.totalAmount||0) + (o.couponDiscount||0)
})
const detailPayAmount = computed(() => {
  const o = orderDetail.value?.orderInfo; if (!o) return 0; return o.payAmount || o.totalAmount || 0
})
const groupedItems = computed(() => {
  const items = orderDetail.value?.orderItems || []
  const groups = []; const pkgMap = new Map()
  for (const item of items) {
    if (item.itemType === 2 && item.packageId) {
      if (!pkgMap.has(item.packageId)) { const g = { type:'package', packageId:item.packageId, packageName: packageNames.value[item.packageId]||'', items:[] }; pkgMap.set(item.packageId, g); groups.push(g) }
      pkgMap.get(item.packageId).items.push(item)
    } else { groups.push({ type:'produce', ...item }) }
  }
  return groups
})
const getStatusType = s => ({ '待发货':'warning','待支付':'warning','已发货':'info','已完成':'success','已取消':'danger' }[s] || 'info')

const fetchOrderList = async () => {
  try { const r = await getOrderList(); if (r.code===200) orders.value = r.data } catch(e){}
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
const handlePay = async (id) => {
  try { payLoading.value[id]=true; const r=await payOrder({orderId:id}); if(r.code===200&&r.data){const d=document.createElement('div');d.innerHTML=r.data;const f=d.querySelector('form');if(f)f.submit()} } catch(e){ElMessage.error('支付失败')} finally { payLoading.value[id]=false }
}
const handleCancel = async (id) => {
  try { cancelLoading.value[id]=true; await cancelOrder(id); ElMessage.success('订单已取消'); fetchOrderList() } catch(e){} finally { cancelLoading.value[id]=false }
}
const handleConfirm = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到货物？', '确认收货', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' })
    await confirmOrder(id); ElMessage.success('已确认收货'); fetchOrderList()
  } catch(e){ if (e !== 'cancel') ElMessage.error(e?.response?.data?.msg || '操作失败') }
}
const viewOrderDetail = async (id) => {
  try { const r=await getOrderDetail(id); if(r.code===200){ orderDetail.value=r.data; const ids=[...new Set((r.data.orderItems||[]).filter(i=>i.packageId).map(i=>i.packageId))]; for(const pid of ids){try{const pr=await getPackageDetail(pid);if(pr.code===200)packageNames.value[pid]=pr.data.packageInfo?.packageName||''}catch(e){}}; detailDialogVisible.value=true } } catch(e){ElMessage.error('获取详情失败')}
}

// ==== 优惠券 ====
const couponTab = ref('available')
const availableList = ref([])
const myList = ref([])
const receiving = ref({})
const claimedIds = ref(new Set())
const typeMap = {1:'满减',2:'立减',3:'折扣'}
const statusMap = {0:'未使用',1:'已使用',2:'已过期'}
const isClaimed = (id) => claimedIds.value.has(id)

const fetchCoupons = async () => {
  const [av,my]=await Promise.all([getAvailableCoupons(),getUserCoupons()])
  if(av.code===200) availableList.value=av.data
  if(my.code===200) claimedIds.value=new Set(my.data.map(c=>c.couponId))
  fetchMyCoupons()
}
const fetchMyCoupons = async () => {
  const r=await getUserCoupons(); if(r.code===200) myList.value=r.data
}
const doReceive = async (id) => {
  receiving.value[id]=true
  try { await receiveCoupon(id); claimedIds.value=new Set([...claimedIds.value,id]); fetchMyCoupons(); fetchCoupons() } catch(e){ ElMessage.error(e?.response?.data?.msg||'领取失败') } finally { receiving.value[id]=false }
}

// ==== 地址 ====
const addresses = ref([])
const dialogVisible = ref(false), dialogTitle = ref('添加地址'), addressFormRef = ref(), loading = ref(false), editingId = ref(null), mapDialogVisible = ref(false)
const addressForm = ref({ recipient:'',phone:'',address:'',isDefault:0 })
const rules = {
  recipient:[{required:true,message:'请输入收件人姓名',trigger:'blur'}],
  phone:[{required:true,message:'请输入联系电话',trigger:'blur'},{pattern:/^1[3-9]\d{9}$/,message:'请输入正确的手机号码',trigger:'blur'}],
  address:[{required:true,message:'请输入详细地址',trigger:'blur'}]
}
const fetchAddressList = async () => { try { const r=await getAddressList(); if(r.code===200) addresses.value=r.data } catch(e){ ElMessage.error('获取地址列表失败') } }
const showAddDialog = () => { dialogTitle.value='添加地址'; editingId.value=null; addressForm.value={recipient:'',phone:'',address:'',isDefault:0}; dialogVisible.value=true }
const showEditDialog = (a) => { dialogTitle.value='编辑地址'; editingId.value=a.id; addressForm.value={recipient:a.recipient,phone:a.phone,address:a.address,isDefault:a.isDefault}; dialogVisible.value=true }
const handleMapConfirm = (d) => { addressForm.value.address=d.fullAddress; mapDialogVisible.value=false; ElMessage.success('地址选择成功') }
const handleSubmit = async () => {
  if(!addressFormRef.value) return
  await addressFormRef.value.validate(async (v) => {
    if(!v) return; loading.value=true
    try { if(editingId.value){await updateAddress(editingId.value,addressForm.value)}else{await addAddress(addressForm.value)}; dialogVisible.value=false; fetchAddressList() } catch(e){ ElMessage.error('操作失败') } finally { loading.value=false }
  })
}
const setDefaultAddress = async (id) => { try { await updateAddress(id,{...addresses.value.find(a=>a.id===id),isDefault:1}); ElMessage.success('已设为默认'); fetchAddressList() } catch(e){ ElMessage.error('设置失败') } }
const deleteAddressHandler = async (id) => { try { await deleteAddress(id); fetchAddressList() } catch(e){ ElMessage.error('删除失败') } }

// ==== 密码 ====
const passwordDialogVisible = ref(false), passwordFormRef = ref(), passwordLoading = ref(false)
const passwordForm = ref({ oldPassword:'',newPassword:'',confirmPassword:'' })
const passwordRules = {
  oldPassword:[{required:true,message:'请输入旧密码',trigger:'blur'}],
  newPassword:[{required:true,message:'请输入新密码',trigger:'blur'},{pattern:/^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/,message:'密码需包含英文字符和数字，长度8-20位',trigger:'blur'}],
  confirmPassword:[{required:true,message:'请确认新密码',trigger:'blur'},{validator:(_,v,cb)=>{cb(v!==passwordForm.value.newPassword?new Error('两次输入密码不一致'):undefined)},trigger:'blur'}]
}
const showPasswordDialog = () => { passwordForm.value={oldPassword:'',newPassword:'',confirmPassword:''}; if(passwordFormRef.value)passwordFormRef.value.clearValidate(); passwordDialogVisible.value=true }
const handlePasswordSubmit = async () => {
  if(!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (v) => { if(!v) return; passwordLoading.value=true; try { await updatePassword(passwordForm.value); ElMessage.success('密码修改成功'); passwordDialogVisible.value=false } catch(e){ ElMessage.error('修改失败') } finally { passwordLoading.value=false } })
}

const roleName = (role) => ({ROLE_ADMIN:'管理员',ROLE_USER:'用户',ROLE_VISITOR:'访客'}[role] || role || '未设置')
const fetchUserInfo = () => { userInfo.value={username:userStore.userInfo.username,role:userStore.userInfo.role,email:userStore.userInfo.email,createdTime:userStore.userInfo.createTime} }

let timer = null
onMounted(() => { fetchUserInfo(); fetchAddressList(); fetchOrderList(); fetchCoupons() })
onUnmounted(() => { if(timer) clearInterval(timer) })
</script>

<style scoped>
.profile-layout { display: flex; gap: 1.5rem; max-width: 1200px; margin: 0 auto; padding: 1.5rem 1rem; min-height: calc(100vh - 140px); }
.profile-sidebar { width: 180px; flex-shrink: 0; }
.sidebar-menu { border-right: none; border-radius: 8px; }
.profile-main { flex: 1; min-width: 0; }

.card-header { font-size: 1.1rem; font-weight: bold; display: flex; justify-content: space-between; align-items: center; }
.user-info { display: flex; flex-direction: column; gap: 0.75rem; }
.info-item { display: flex; align-items: center; gap: 0.75rem; }
.info-item .label { width: 90px; font-weight: bold; color: #606266; flex-shrink: 0; }
.info-item .value { color: #303133; }

.address-header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.address-list { display: flex; flex-direction: column; gap: 0.75rem; }
.address-item { transition: transform 0.3s; }
.address-item:hover { transform: translateY(-3px); }
.address-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; flex-wrap: wrap; gap: 0.5rem; }
.recipient-info { display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap; }
.recipient-name { font-weight: bold; color: #303133; }
.recipient-phone { color: #606266; }
.address-operations { display: flex; gap: 0.4rem; flex-wrap: wrap; }
.address-content { color: #606266; line-height: 1.5; }

/* 订单 */
.order-list { display: flex; flex-direction: column; gap: 0.75rem; }
.order-card { transition: transform 0.3s; }
.order-card:hover { transform: translateY(-2px); }
.order-card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 0.5rem; padding-bottom: 0.5rem; border-bottom: 1px solid #ebeef5; }
.order-primary { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
.order-no { font-weight: bold; color: #303133; font-size: 0.9rem; }
.order-amount { font-size: 1.2rem; font-weight: bold; color: #f56c6c; }
.order-card-body { margin-bottom: 0.5rem; }
.order-meta { display: flex; flex-wrap: wrap; gap: 0.4rem 1.5rem; font-size: 0.85rem; }
.meta-label { color: #909399; margin-right: 0.35rem; }
.meta-value { color: #606266; }
.order-card-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; padding-top: 0.5rem; border-top: 1px solid #ebeef5; }

/* 优惠券 */
.coupon-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; }
.coupon-card { display: flex; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.06); background: #fff; }
.coupon-card.disabled,.coupon-card.used { opacity: 0.6; }
.coupon-left { width: 100px; background: linear-gradient(135deg,#3EB135,#328E2A); color: #fff; display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 0.75rem; flex-shrink: 0; }
.coupon-type-tag { font-size: 0.75rem; opacity: 0.85; margin-bottom: 0.15rem; }
.coupon-value .symbol { font-size: 0.85rem; }
.coupon-value .num { font-size: 1.6rem; font-weight: bold; }
.coupon-value .unit { font-size: 0.85rem; }
.coupon-condition { font-size: 0.75rem; opacity: 0.85; }
.coupon-right { flex: 1; padding: 0.75rem 1rem; display: flex; flex-direction: column; justify-content: center; gap: 0.3rem; min-width: 0; }
.coupon-name { font-weight: bold; font-size: 0.95rem; color: #303133; }
.coupon-time { font-size: 0.8rem; color: #909399; }
.coupon-count { font-size: 0.8rem; color: #606266; }

/* 弹窗 */
.form-item-extra { margin-top: 0.5rem; display: flex; justify-content: flex-end; }
.contact-us { text-align: center; margin-top: 1.5rem; padding: 1rem 0; }
.contact-body { text-align: center; padding: 1rem 0; }
.contact-text { color: #606266; margin: 0 0 1rem; }
.contact-email { font-size: 1.2rem; font-weight: bold; color: #3EB135; padding: 0.75rem 1.5rem; background: #ECF7EB; border-radius: 8px; display: inline-block; }
:deep(.address-dialog) { max-width: 500px; }
:deep(.password-dialog) { max-width: 500px; }
:deep(.map-dialog) { max-width: 900px; }
:deep(.order-detail-dialog-wrapper) { max-width: 800px; }

/* 订单详情 */
.dialog-section { margin-bottom: 1rem; padding-bottom: 0.75rem; border-bottom: 1px solid #ebeef5; }
.dialog-section h4 { margin: 0 0 0.75rem; color: #303133; font-size: 1rem; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; }
.info-item .value.price { color: #f56c6c; font-weight: bold; }
.address-info { display: flex; flex-direction: column; gap: 0.6rem; }
.dialog-footer { margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid #ebeef5; text-align: right; }
.total-label { font-weight: bold; color: #303133; font-size: 1rem; }
.total-price { color: #f56c6c; font-weight: bold; font-size: 1.2rem; }

.order-items-grouped { display: flex; flex-direction: column; }
.pkg-group { border: 1px solid #C5E8C2; border-radius: 6px; overflow: hidden; margin-bottom: 0.4rem; }
.pkg-group-header { display: flex; align-items: center; gap: 0.4rem; padding: 0.35rem 0.6rem; background: #ECF7EB; font-weight: bold; color: #3EB135; font-size: 0.9rem; }
.pkg-group-name { flex: 1; }
.pkg-group-items { padding: 0.15rem 0; }
.order-item-row { display: flex; align-items: center; gap: 0.75rem; padding: 0.3rem 0.6rem; border-bottom: 1px solid #ebeef5; font-size: 0.9rem; }
.order-item-row:last-child { border-bottom: none; }
.order-item-row.single-produce { padding: 0.4rem 0.6rem; background: #fafafa; border-radius: 4px; margin-bottom: 0.3rem; }
.item-name { flex: 1; } .item-name.indent { padding-left: 1rem; }
.item-price { width: 70px; color: #606266; text-align: right; }
.item-qty { width: 40px; color: #909399; text-align: center; }
.item-subtotal { width: 80px; color: #f56c6c; font-weight: bold; text-align: right; }

@media (max-width: 768px) {
  .profile-layout { flex-direction: column; padding: 0.75rem 0.5rem; }
  .profile-sidebar { width: 100%; }
  .sidebar-menu { display: flex; overflow-x: auto; }
  .sidebar-menu .el-menu-item { flex-shrink: 0; min-width: auto; padding: 0 0.75rem; }
  .coupon-grid { grid-template-columns: 1fr; }
  .info-grid { grid-template-columns: 1fr; }
}
</style>
