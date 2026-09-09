<template>
  <div class="pkg-detail">
    <div v-if="pkg">
      <div class="detail-top">
        <div class="cover"><el-image :src="pkg.packageInfo.coverImg || 'https://via.placeholder.com/400x300'" fit="cover" /></div>
        <div class="info">
          <h2>{{ pkg.packageInfo.packageName }}</h2>
          <p class="desc">{{ pkg.packageInfo.packageDesc }}</p>
          <div class="prices">
            <span class="original">原价 ¥{{ pkg.packageInfo.originalPrice }}</span>
            <span class="price">套餐价 ¥{{ pkg.packageInfo.packagePrice }}</span>
          </div>
          <div class="actions">
            <el-input-number v-model="qty" :min="1" size="large" />
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
          </div>
        </div>
      </div>

      <div class="produce-section">
        <h3>套餐包含农产品</h3>
        <div class="produce-grid">
          <el-card v-for="b in pkg.produces" :key="b.id" shadow="hover" class="produce-item" @click="goToProduce(b.id)">
            <div class="produce-cover"><el-image :src="b.coverUrl" fit="cover" /></div>
            <div class="produce-name">{{ b.produceName }}</div>
            <div class="produce-price">单价 ¥{{ b.price }} × {{ b.stock }}</div>
          </el-card>
        </div>
      </div>
    </div>
    <el-empty v-else description="加载中..." />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getPackageDetail } from '@/apis/package.js'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const pkg = ref(null)
const qty = ref(1)

onMounted(async () => {
  const res = await getPackageDetail(route.params.id)
  if (res.code === 200) pkg.value = res.data
})

const goToProduce = (id) => router.push(`/produce/${id}`)

const addToCart = async () => {
  if (!tokenStore.token) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    const res = await request.post('/cart', { packageId: pkg.value.packageInfo.id, quantity: qty.value, cartType: 2 })
    if (res.code === 200) ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '添加失败')
  }
}
</script>

<style scoped>
.pkg-detail { max-width: 1000px; margin: 0 auto; padding: 0 1rem; }
.detail-top { display: flex; gap: 2rem; margin-bottom: 2rem; }
.cover { width: 350px; height: 260px; flex-shrink: 0; border-radius: 8px; overflow: hidden; }
.info { flex: 1; display: flex; flex-direction: column; gap: 1rem; }
.info h2 { margin: 0; }
.desc { color: #606266; line-height: 1.6; }
.prices { display: flex; align-items: baseline; gap: 1rem; }
.prices .original { text-decoration: line-through; color: #c0c4cc; font-size: 1rem; }
.prices .price { color: #f56c6c; font-size: 1.8rem; font-weight: bold; }
.actions { display: flex; gap: 1rem; }
.produce-section h3 { margin-bottom: 1rem; }
.produce-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; }
.produce-item { text-align: center; cursor: pointer; transition: transform 0.3s; }
.produce-item:hover { transform: translateY(-3px); }
.produce-cover { height: 140px; margin-bottom: 0.5rem; }
.produce-cover .el-image { width: 100%; height: 100%; }
.produce-name { font-weight: bold; font-size: 0.9rem; }
.produce-price { color: #909399; font-size: 0.85rem; }

@media (max-width: 768px) {
  .detail-top { flex-direction: column; }
  .cover { width: 100%; height: 200px; }
  .produce-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
