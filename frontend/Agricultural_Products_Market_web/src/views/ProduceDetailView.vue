<template>
  <div class="produce-detail-container">
    <el-card v-if="produce" shadow="hover">
      <div class="produce-detail-content">
        <div class="produce-cover-large">
          <el-image :src="produce.coverUrl || 'https://via.placeholder.com/400'" :fit="'cover'" />
        </div>
        <div class="produce-info-detail">
          <h2 class="produce-title-large">{{ produce.produceName }}</h2>
          <div class="produce-meta">
            <span class="meta-item">生产商：{{ produce.manufacturer }}</span>
            <span class="meta-item">分类：{{ produce.categoryName }}</span>
            <span class="meta-item" :class="{ 'status-active': produce.status === 1, 'status-inactive': produce.status === 0 }">
              {{ produce.status === 1 ? '上架' : '下架' }}
            </span>
          </div>
          <div class="produce-price-large">¥{{ produce.price }}</div>
          <div class="produce-stock-large">库存：{{ produce.stock }} 本</div>
          <div class="produce-description">
            <h3>农产品简介</h3>
            <p>{{ produce.description || '暂无简介' }}</p>
          </div>
          <div class="produce-actions">
            <el-input-number v-model="quantity" :min="1" :max="produce.stock" :step="1" class="quantity-input" />
            <el-button type="primary" @click="addToCartHandler" :disabled="produce.status === 0" :loading="loading" class="add-to-cart-btn">
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
    <el-empty v-else description="加载中..." />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getProduceDetail } from '@/apis/produce.js'
import { addToCart } from '@/apis/cart.js'
import { categoryCache } from '@/stores/category.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const token = tokenStore.token

const produceId = route.params.id
const produce = ref(null)
const loading = ref(false)
const quantity = ref(1)

// 获取农产品详情
const fetchProduceDetail = async () => {
  try {
    const response = await getProduceDetail(produceId)
    if (response.code === 200) {
      produce.value = response.data
      // 存储分类信息供面包屑使用
      categoryCache.categoryName = response.data.categoryName || ''
      categoryCache.categoryId = response.data.categoryId || null
    }
  } catch (error) {
    console.error('获取农产品详情失败:', error)
  }
}

// 添加到购物车
const addToCartHandler = async () => {
  if (!token) {
    ElMessage.warning('请先登录')
    await router.push('/login')
    return
  }
  
  loading.value = true
  try {
    const response = await addToCart({
      produceId: produce.value.id,
      quantity: quantity.value
    })
    if (response.code === 200) {
      ElMessage.success('添加购物车成功')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
  } finally {
    loading.value = false
  }
}

// 立即购买
const buyNow = () => {
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  // 这里可以跳转到结算页面，暂时先加入购物车然后跳转到购物车
  addToCartHandler().then(() => {
    router.push('/cart')
  })
}

// 初始化
onMounted(() => {
  fetchProduceDetail()
})

onUnmounted(() => {
  categoryCache.categoryName = ''
  categoryCache.categoryId = null
})
</script>

<style scoped>
.produce-detail-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.produce-detail-content {
  display: flex;
  gap: 2rem;
}

.produce-cover-large {
  width: 400px;
  max-width: 100%;
  height: auto;
  aspect-ratio: 4 / 5;
  flex-shrink: 0;
}

.produce-cover-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.produce-info-detail {
  flex: 1;
}

.produce-title-large {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #303133;
}

.produce-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  color: #606266;
}

.meta-item {
  padding: 0.3rem 0.8rem;
  background-color: #f5f7fa;
  border-radius: 16px;
  font-size: 0.9rem;
}

.status-active {
  color: #67c23a;
  background-color: #f0f9eb;
}

.status-inactive {
  color: #909399;
  background-color: #f5f7fa;
}

.produce-price-large {
  font-size: 2.5rem;
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 1rem;
}

.produce-stock-large {
  font-size: 1.1rem;
  color: #67c23a;
  margin-bottom: 2rem;
}

.produce-description {
  margin-bottom: 2rem;
}

.produce-description h3 {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: #303133;
}

.produce-description p {
  color: #606266;
  line-height: 1.8;
  text-indent: 2em;
}

.produce-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-input {
  width: 120px;
}

.add-to-cart-btn,
.buy-now-btn {
  padding: 0 2rem;
  height: 40px;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .produce-detail-container {
    padding: 0 0.5rem;
    margin: 1rem auto;
  }

  .produce-detail-content {
    flex-direction: column;
  }

  .produce-cover-large {
    width: 100%;
    max-width: 100%;
    aspect-ratio: 3 / 4;
  }

  .produce-title-large {
    font-size: 1.5rem;
  }

  .produce-price-large {
    font-size: 1.8rem;
  }

  .produce-meta {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .produce-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .quantity-input {
    width: 100%;
  }

  .add-to-cart-btn,
  .buy-now-btn {
    width: 100%;
  }
}
</style>