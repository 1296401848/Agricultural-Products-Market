<template>
  <div class="coupon-center">
    <h2 class="page-title">领券中心</h2>

    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" @tab-change="fetchMyCoupons">
      <el-tab-pane label="可领优惠券" name="available" />
      <el-tab-pane label="我的优惠券" name="mine" />
    </el-tabs>

    <!-- 可领优惠券 -->
    <div v-if="activeTab === 'available'" class="coupon-grid">
      <div v-for="c in availableList" :key="c.id" class="coupon-card" :class="{ disabled: c.usedCount >= c.totalCount }">
        <div class="coupon-left">
          <div class="coupon-type-tag">{{ typeMap[c.couponType] }}</div>
          <div class="coupon-value">
            <template v-if="c.couponType === 3">
              <span class="num">{{ (c.discount * 10).toFixed(0) }}</span><span class="unit">折</span>
            </template>
            <template v-else>
              <span class="symbol">¥</span><span class="num">{{ c.reduceMoney }}</span>
            </template>
          </div>
          <div class="coupon-condition" v-if="c.couponType === 1">满{{ c.fullMoney }}元可用</div>
          <div class="coupon-condition" v-else-if="c.couponType === 2">无门槛</div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.couponName }}</div>
          <div class="coupon-time">领取后{{ c.validDays }}天有效</div>
          <div class="coupon-count">已领 {{ c.usedCount }}/{{ c.totalCount }}</div>
          <el-button
            type="primary" size="small"
            @click="doReceive(c.id)"
            :loading="receiving[c.id]"
            :disabled="isClaimed(c.id) || c.usedCount >= c.totalCount"
          >
            {{ isClaimed(c.id) ? '已领取' : c.usedCount >= c.totalCount ? '已抢光' : '立即领取' }}
          </el-button>
        </div>
      </div>
      <el-empty v-if="availableList.length === 0" description="暂无可领优惠券" />
    </div>

    <!-- 我的优惠券 -->
    <div v-if="activeTab === 'mine'" class="coupon-grid">
      <div v-for="c in myList" :key="c.id" class="coupon-card" :class="{ used: c.useStatus !== 0 }">
        <div class="coupon-left">
          <div class="coupon-type-tag">{{ typeMap[c.couponType] }}</div>
          <div class="coupon-value">
            <template v-if="c.couponType === 3">
              <span class="num">{{ (c.discount * 10).toFixed(0) }}</span><span class="unit">折</span>
            </template>
            <template v-else>
              <span class="symbol">¥</span><span class="num">{{ c.reduceMoney }}</span>
            </template>
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ c.couponName }}</div>
          <div class="coupon-time">{{ c.validStart }} ~ {{ c.validEnd }}</div>
          <el-tag :type="c.useStatus === 0 ? 'primary' : c.useStatus === 1 ? 'info' : 'warning'" size="small">
            {{ statusMap[c.useStatus] }}
          </el-tag>
        </div>
      </div>
      <el-empty v-if="myList.length === 0" description="暂无优惠券" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAvailableCoupons, receiveCoupon, getUserCoupons } from '@/apis/coupon.js'
import { ElMessage } from 'element-plus'

const activeTab = ref('available')
const availableList = ref([])
const myList = ref([])
const receiving = ref({})
const claimedIds = ref(new Set())

const typeMap = { 1: '满减', 2: '立减', 3: '折扣' }
const statusMap = { 0: '未使用', 1: '已使用', 2: '已过期' }

const fetchAvailable = async () => {
  const [avRes, myRes] = await Promise.all([
    getAvailableCoupons(),
    getUserCoupons()
  ])
  if (avRes.code === 200) availableList.value = avRes.data
  if (myRes.code === 200) claimedIds.value = new Set(myRes.data.map(c => c.couponId))
  fetchMyCoupons()
}

const fetchMyCoupons = async () => {
  if (activeTab.value !== 'mine') return
  const res = await getUserCoupons()
  if (res.code === 200) myList.value = res.data
}

const isClaimed = (couponId) => claimedIds.value.has(couponId)

const doReceive = async (couponId) => {
  receiving.value[couponId] = true
  try {
    await receiveCoupon(couponId)
    claimedIds.value = new Set([...claimedIds.value, couponId])
    fetchMyCoupons()
    fetchAvailable()
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '领取失败')
  } finally {
    receiving.value[couponId] = false
  }
}

onMounted(() => fetchAvailable())
</script>

<style scoped>
.coupon-center { max-width: 1200px; margin: 0 auto; padding: 0 1rem; }
.page-title { font-size: 1.5rem; margin-bottom: 1rem; color: #303133; }
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}
.coupon-card {
  display: flex; border-radius: 10px; overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08); background: #fff;
  min-width: 0;
}
.coupon-card.disabled { opacity: 0.6; }
.coupon-card.used { opacity: 0.7; filter: grayscale(30%); }
.coupon-left {
  width: 120px; background: linear-gradient(135deg, #f56c6c, #e64242);
  color: #fff; display: flex; flex-direction: column;
  justify-content: center; align-items: center; padding: 1rem; flex-shrink: 0;
}
.coupon-type-tag { font-size: 0.8rem; opacity: 0.85; margin-bottom: 0.25rem; }
.coupon-value { text-align: center; }
.coupon-value .symbol { font-size: 1rem; }
.coupon-value .num { font-size: 2rem; font-weight: bold; }
.coupon-value .unit { font-size: 1rem; }
.coupon-condition { font-size: 0.8rem; margin-top: 0.25rem; opacity: 0.85; }
.coupon-right {
  flex: 1; padding: 1rem 1.25rem; display: flex;
  flex-direction: column; justify-content: center; gap: 0.4rem;
  min-width: 0;
}
.coupon-name { font-weight: bold; font-size: 1.05rem; color: #303133; }
.coupon-time { font-size: 0.85rem; color: #909399; }
.coupon-count { font-size: 0.85rem; color: #606266; }

/* 响应式 */
@media (max-width: 768px) {
  .coupon-grid {
    grid-template-columns: 1fr;
  }
  .coupon-left {
    width: 100px;
    padding: 0.75rem;
  }
  .coupon-value .num { font-size: 1.5rem; }
  .coupon-right { padding: 0.75rem 1rem; }
}
</style>
