<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-box">
        <el-input v-model="keyword" placeholder="搜索农产品名称或生产商" prefix-icon="Search" size="large" @keyup.enter="doSearch" clearable @clear="doSearch">
          <template #append>
            <el-button @click="doSearch" type="primary">搜索</el-button>
          </template>
        </el-input>
      </div>
      <div class="filter-wrapper">
        <el-button size="large" class="filter-btn" @click="filterVisible = !filterVisible">
          <el-icon><Filter /></el-icon>筛选
          <el-badge v-if="activeFilterCount > 0" :value="activeFilterCount" type="danger" class="filter-badge" />
        </el-button>
        <div v-if="filterVisible" class="filter-backdrop" @click="filterVisible = false"></div>
        <div v-show="filterVisible" class="filter-dropdown" @click.stop>
          <div class="filter-group">
            <label class="filter-label">农产品分类</label>
            <el-select v-model="filterCategory" placeholder="全部分类" multiple collapse-tags clearable style="width:100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
            </el-select>
          </div>
          <div class="filter-group">
            <label class="filter-label">价格范围</label>
            <div class="price-range"><el-input v-model="filterMinPrice" placeholder="最低价" size="small" /><span class="price-sep">—</span><el-input v-model="filterMaxPrice" placeholder="最高价" size="small" /></div>
          </div>
          <div class="filter-group">
            <label class="filter-label">库存状态</label>
            <el-radio-group v-model="filterInStock"><el-radio :value="null">全部</el-radio><el-radio :value="1">仅看有货</el-radio></el-radio-group>
          </div>
          <div class="filter-actions"><el-button @click="resetFilter" size="small">重置</el-button><el-button type="primary" @click="applyFilter" size="small">应用筛选</el-button></div>
        </div>
      </div>
    </div>

    <!-- 结果统计 -->
    <div class="result-info" v-if="total > 0">
      搜索 <strong>"{{ route.query.keyword || '' }}"</strong>，共找到 <strong>{{ total }}</strong> 件农产品
    </div>

    <!-- 农产品列表 -->
    <div class="produce-list">
      <el-card v-for="produce in produces" :key="produce.id" class="produce-card" @click="$router.push('/produce/' + produce.id)">
        <template #header><div class="produce-title">{{ produce.produceName }}</div></template>
        <div class="produce-cover"><el-image :src="produce.coverUrl" :fit="'cover'" /></div>
        <div class="produce-info">
          <div class="produce-manufacturer">生产商：{{ produce.manufacturer }}</div>
          <div class="produce-price">¥{{ produce.price }}</div>
          <div class="produce-stock">库存：{{ produce.stock }}</div>
        </div>
        <el-button type="primary" @click.stop="addToCartHandler(produce.id)" v-if="token">加入购物车</el-button>
      </el-card>
    </div>

    <el-empty v-if="searched && produces.length === 0" description="未找到相关农产品" :image-size="160" />

    <div class="pagination" v-if="total > 0">
      <el-pagination v-model:current-page="page" :page-size="size" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" :total="total" @size-change="fetchList" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getProduceList } from '@/apis/produce.js'
import { addToCart } from '@/apis/cart.js'
import { getCategoryList } from '@/apis/category.js'
import { ElMessage } from 'element-plus'
import { Filter } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const token = tokenStore.token

const keyword = ref(route.query.keyword || '')
const page = ref(1), size = ref(10), total = ref(0), produces = ref([]), searched = ref(false)
const categories = ref([])
const filterVisible = ref(false)
const filterCategory = ref([])
const filterMinPrice = ref(''), filterMaxPrice = ref(''), filterInStock = ref(null)

const activeFilterCount = computed(() => {
  let c = 0
  if (filterCategory.value.length) c++
  if (filterMinPrice.value || filterMaxPrice.value) c++
  if (filterInStock.value !== null) c++
  return c
})

const fetchList = async () => {
  const params = { produceName: keyword.value || '', currentPage: page.value, pageSize: size.value }
  // 优先使用 URL 参数中的分类
  if (route.query.categoryId) filterCategory.value = [Number(route.query.categoryId)]
  if (filterCategory.value.length) params.categoryIds = filterCategory.value.join(',')
  if (filterMinPrice.value) params.minPrice = filterMinPrice.value
  if (filterMaxPrice.value) params.maxPrice = filterMaxPrice.value
  if (filterInStock.value !== null) params.inStock = filterInStock.value
  const res = await getProduceList(params)
  if (res.code === 200) { produces.value = res.data.list; total.value = res.data.total }
  searched.value = true
}

const doSearch = () => {
  page.value = 1
  router.replace({ query: { ...route.query, keyword: keyword.value || undefined } })
  fetchList()
}
const applyFilter = () => { page.value = 1; filterVisible.value = false; fetchList() }
const resetFilter = () => { filterCategory.value = []; filterMinPrice.value = ''; filterMaxPrice.value = ''; filterInStock.value = null; page.value = 1; fetchList() }

const addToCartHandler = async (produceId) => {
  try { await addToCart({ produceId, quantity: 1 }); ElMessage.success('已加入购物车') } catch(e) {}
}

// 从 URL 参数初始化筛选条件
if (route.query.categoryId) filterCategory.value = [Number(route.query.categoryId)]
if (route.query.categoryIds) filterCategory.value = route.query.categoryIds.split(',').map(Number)
if (route.query.minPrice) filterMinPrice.value = route.query.minPrice
if (route.query.maxPrice) filterMaxPrice.value = route.query.maxPrice
if (route.query.inStock) filterInStock.value = Number(route.query.inStock)

onMounted(async () => {
  const cr = await getCategoryList(); if (cr.code === 200) categories.value = cr.data
  fetchList()
})

watch(() => route.query.categoryId, (v) => {
  if (v) {
    filterCategory.value = [Number(v)]
    page.value = 1
    fetchList()
  }
})
watch(() => route.query.keyword, (v) => { if (v) { keyword.value = v; doSearch() } })
</script>

<style scoped>
.search-page { max-width: 1200px; margin: 0 auto; padding: 0 1rem; }
.search-bar { display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1.25rem; }
.search-box { flex: 1; min-width: 0; }
.result-info { margin-bottom: 1rem; color: #606266; font-size: 0.95rem; }
.produce-list { display: grid; grid-template-columns: repeat(5, 1fr); gap: 1rem; margin-bottom: 1.5rem; }
.produce-card { cursor: pointer; transition: transform 0.3s; }
.produce-card:hover { transform: translateY(-3px); }
.produce-cover { height: 160px; margin-bottom: 0.5rem; border-radius: 6px; overflow: hidden; }
.produce-cover .el-image { width: 100%; height: 100%; display: block; }
.produce-title { font-weight: bold; font-size: 0.9rem; line-height: 1.2; }
.produce-manufacturer { color: #606266; font-size: 0.8rem; margin-bottom: 0.2rem; }
.produce-price { color: #f56c6c; font-weight: bold; font-size: 0.9rem; margin-bottom: 0.2rem; }
.produce-stock { color: #67c23a; font-size: 0.8rem; }
.pagination { display: flex; justify-content: center; margin-top: 2rem; }

.filter-wrapper { position: relative; flex-shrink: 0; }
.filter-backdrop { position: fixed; inset: 0; z-index: 999; }
.filter-dropdown { position: absolute; right: 0; top: 100%; margin-top: 4px; width: 320px; background: #fff; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.12); padding: 1.25rem; z-index: 1000; display: flex; flex-direction: column; gap: 1rem; }
.filter-group { display: flex; flex-direction: column; gap: 0.5rem; }
.filter-label { font-size: 0.9rem; font-weight: bold; color: #303133; }
.price-range { display: flex; align-items: center; gap: 0.5rem; }
.price-range .el-input { flex: 1; min-width: 0; }
.price-sep { color: #909399; }
.filter-actions { display: flex; justify-content: flex-end; gap: 0.5rem; padding-top: 0.5rem; border-top: 1px solid #ebeef5; }

@media (max-width: 768px) {
  .produce-list { grid-template-columns: repeat(2, 1fr); }
  .search-bar { flex-direction: column; }
  .filter-wrapper { width: 100%; }
  .filter-btn { width: 100%; }
  .filter-dropdown { width: 100%; left: 0; right: auto; }
}
@media (max-width: 480px) { .produce-list { grid-template-columns: 1fr; } }
</style>
