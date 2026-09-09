<template>
  <div class="produce-component">
    <!-- 推荐列表 -->
    <div class="recommend-section" v-if="recommendedProduces.length > 0">
      <div class="recommend-header">
        <h3 class="recommend-title">随便看看</h3>
        <el-button type="primary" link @click="refreshRecommend">换一批</el-button>
      </div>
      <div class="recommend-list">
        <el-card v-for="produce in recommendedProduces" :key="produce.id" class="recommend-card" shadow="hover" @click="goToProduceDetail(produce.id)">
          <div class="recommend-cover">
            <el-image :src="produce.coverUrl" :fit="'cover'" />
          </div>
          <div class="recommend-info">
            <div class="recommend-produce-name">{{ produce.produceName }}</div>
            <div class="recommend-produce-price">¥{{ produce.price }}</div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 套餐推荐 -->
    <div class="recommend-section package-section" v-if="packages.length > 0">
      <div class="recommend-header">
        <h3 class="recommend-title">超值套餐</h3>
        <el-button type="primary" link @click="$router.push('/package')">更多 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      <div class="recommend-list">
        <el-card v-for="pkg in packages" :key="pkg.id" class="recommend-card" shadow="hover" @click="$router.push('/package/' + pkg.id)">
          <div class="recommend-cover">
            <el-image :src="pkg.coverImg || 'https://via.placeholder.com/280x180'" :fit="'cover'" />
          </div>
          <div class="recommend-info">
            <div class="recommend-produce-name">{{ pkg.packageName }}</div>
            <div class="recommend-produce-price" style="text-decoration:line-through;color:#c0c4cc;font-size:0.8rem">¥{{ pkg.originalPrice }}</div>
            <div class="recommend-produce-price">¥{{ pkg.packagePrice }}</div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="search-bar">
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索农产品名称或生产商" prefix-icon="Search" size="large" @keyup.enter="handleSearch" clearable @clear="handleSearch">
          <template #append>
            <el-button @click="handleSearch" type="primary">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-wrapper">
        <el-button size="large" class="filter-btn" @click="filterVisible = !filterVisible">
          <el-icon><Filter /></el-icon>
          筛选
          <el-badge v-if="hasActiveFilter" :value="activeFilterCount" type="danger" class="filter-badge" />
        </el-button>

        <!-- 点击遮罩关闭 -->
        <div v-if="filterVisible" class="filter-backdrop" @click="filterVisible = false"></div>

        <div v-show="filterVisible" class="filter-dropdown" @click.stop>
          <div class="filter-group">
            <label class="filter-label">农产品分类</label>
            <el-select v-model="filterCategory" placeholder="全部分类" multiple collapse-tags collapse-tags-tooltip clearable style="width: 100%" popper-class="filter-select-dropdown">
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.categoryName"
                :value="cat.id"
              />
            </el-select>
          </div>

          <div class="filter-group">
            <label class="filter-label">价格范围</label>
            <div class="price-range">
              <el-input v-model="filterMinPrice" placeholder="最低价" size="small" />
              <span class="price-sep">—</span>
              <el-input v-model="filterMaxPrice" placeholder="最高价" size="small" />
            </div>
          </div>

          <div class="filter-group">
            <label class="filter-label">库存状态</label>
            <el-radio-group v-model="filterInStock">
              <el-radio :value="null">全部</el-radio>
              <el-radio :value="1">仅看有货</el-radio>
            </el-radio-group>
          </div>

          <div class="filter-actions">
            <el-button @click="resetFilter" size="small">重置</el-button>
            <el-button type="primary" @click="applyFilterAndClose" size="small">应用筛选</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 农产品列表 -->
    <div class="produce-list">
      <el-card v-for="produce in produces" :key="produce.id" class="produce-card" @click="goToProduceDetail(produce.id)">
        <template #header>
          <div class="produce-title">{{ produce.produceName }}</div>
        </template>
        <div class="produce-cover">
          <el-image :src="produce.coverUrl" :fit="'cover'" style="width: 160px"/>
        </div>
        <div class="produce-info">
          <div class="produce-manufacturer">生产商：{{ produce.manufacturer }}</div>
          <div class="produce-price">价格：¥{{ produce.price }}</div>
          <div class="produce-stock">库存：{{ produce.stock }}</div>
        </div>
        <el-button type="primary" @click.stop="addToCartHandler(produce.id)" v-if="token">加入购物车</el-button>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { getProduceList } from '@/apis/produce.js'
import { addToCart } from '@/apis/cart.js'
import { getCategoryList } from '@/apis/category.js'
import { getPackageList } from '@/apis/package.js'
import { ElMessage } from 'element-plus'
import { Filter, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const tokenStore = useTokenStore()
const token = tokenStore.token

// 搜索和分页
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const produces = ref([])

// 筛选面板显示
const filterVisible = ref(false)

// 筛选状态
const filterCategory = ref([])
const filterMinPrice = ref('')
const filterMaxPrice = ref('')
const filterInStock = ref(null)

// 筛选 badge 计数
const activeFilterCount = computed(() => {
  let count = 0
  if (filterCategory.value.length > 0) count++
  if (filterMinPrice.value || filterMaxPrice.value) count++
  if (filterInStock.value !== null) count++
  return count
})

const hasActiveFilter = computed(() => activeFilterCount.value > 0)

// 套餐推荐
const packages = ref([])
const fetchPackages = async () => {
  try {
    const res = await getPackageList()
    if (res.code === 200) packages.value = (res.data || []).slice(0, 6)
  } catch (e) { /* ignore */ }
}

// 推荐农产品
const recommendedProduces = ref([])
const allProducesCache = ref([])

// 从缓存中随机选取 6 本
const pickRandomProduces = () => {
  if (allProducesCache.value.length === 0) return
  const shuffled = [...allProducesCache.value].sort(() => Math.random() - 0.5)
  recommendedProduces.value = shuffled.slice(0, 6)
}

// 刷新推荐
const refreshRecommend = () => {
  pickRandomProduces()
}

// 获取推荐农产品数据
const fetchRecommendedProduces = async () => {
  try {
    const response = await getProduceList({
      currentPage: 1,
      pageSize: 50
    })
    if (response.code === 200 && response.data.list.length > 0) {
      allProducesCache.value = response.data.list
      pickRandomProduces()
    }
  } catch (error) {
    console.error('获取推荐农产品失败:', error)
  }
}

// 分类列表（供筛选下拉使用）
const categories = ref([])

// 获取分类列表
const fetchCategoryList = async () => {
  try {
    const response = await getCategoryList()
    if (response.code === 200) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取农产品列表
const fetchProduceList = async () => {
  try {
    const params = {
      produceName: searchKeyword.value,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterCategory.value.length > 0) params.categoryIds = filterCategory.value.join(',')
    if (filterMinPrice.value) params.minPrice = filterMinPrice.value
    if (filterMaxPrice.value) params.maxPrice = filterMaxPrice.value
    if (filterInStock.value !== null) params.inStock = filterInStock.value

    const response = await getProduceList(params)
    if (response.code === 200) {
      produces.value = response.data.list
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取农产品列表失败:', error)
  }
}

// 搜索 → 跳转搜索页
const handleSearch = () => {
  const q = searchKeyword.value.trim()
  if (q) {
    router.push({ path: '/search', query: { keyword: q } })
  }
}

// 应用筛选 → 跳转搜索页
const applyFilter = () => {
  const query = {}
  if (searchKeyword.value) query.keyword = searchKeyword.value
  if (filterCategory.value.length) query.categoryIds = filterCategory.value.join(',')
  if (filterMinPrice.value) query.minPrice = filterMinPrice.value
  if (filterMaxPrice.value) query.maxPrice = filterMaxPrice.value
  if (filterInStock.value !== null) query.inStock = filterInStock.value
  router.push({ path: '/search', query })
}

// 应用筛选并关闭面板
const applyFilterAndClose = () => {
  applyFilter()
  filterVisible.value = false
}

// 重置筛选
const resetFilter = () => {
  filterCategory.value = []
  filterMinPrice.value = ''
  filterMaxPrice.value = ''
  filterInStock.value = null
  currentPage.value = 1
  fetchProduceList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchProduceList()
}

// 当前页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProduceList()
}

// 跳转到农产品详情
const goToProduceDetail = (id) => {
  router.push(`/produce/${id}`)
}

// 添加到购物车
const addToCartHandler = async (produceId) => {
  try {
    const response = await addToCart({
      produceId,
      quantity: 1
    })
    if (response.code === 200) {
      ElMessage.success('添加购物车成功')
    }
  } catch (error) {
    console.error('添加购物车失败:', error)
  }
}

// 初始化
onMounted(async () => {
  await fetchCategoryList()
  fetchProduceList()
  fetchRecommendedProduces()
  fetchPackages()
})
</script>

<style scoped>
.produce-component {
  width: 100%;
}

/* 推荐列表样式 */
.recommend-section {
  margin-bottom: 2rem;
  padding: 1.25rem;
  background: linear-gradient(135deg, #fdf6ec 0%, #fef9f0 100%);
  border-radius: 12px;
  border: 1px solid #fae3c4;
}

.package-section {
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f3ff 100%);
  border-color: #b3d8ff;
}
.package-section .recommend-title {
  color: #409eff;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.recommend-title {
  margin: 0;
  font-size: 1.15rem;
  color: #e6a23c;
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.75rem;
}

.recommend-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.recommend-card:hover {
  transform: translateY(-3px);
}

.recommend-card :deep(.el-card__body) {
  padding: 0.75rem;
}

.recommend-cover {
  height: 140px;
  margin-bottom: 0.5rem;
  border-radius: 6px;
  overflow: hidden;
}

.recommend-cover .el-image {
  width: 100%;
  height: 100%;
}

.recommend-info {
  text-align: center;
}

.recommend-produce-name {
  font-size: 0.85rem;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 0.25rem;
}

.recommend-produce-price {
  font-size: 0.9rem;
  color: #f56c6c;
  font-weight: bold;
}

/* 搜索和筛选栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.search-box {
  flex: 1;
  min-width: 0;
}

.filter-btn {
  flex-shrink: 0;
}

.filter-badge {
  margin-left: 0.25rem;
}

/* 筛选下拉面板 */
.filter-wrapper {
  position: relative;
}

.filter-backdrop {
  position: fixed;
  inset: 0;
  z-index: 999;
}

.filter-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 4px;
  width: 320px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 1.25rem;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.9rem;
  font-weight: bold;
  color: #303133;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price-range .el-input {
  flex: 1;
  min-width: 0;
}

.price-sep {
  color: #909399;
  flex-shrink: 0;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid #ebeef5;
}

.produce-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.produce-card {
  cursor: pointer;
  transition: transform 0.3s;
  padding: 0.5rem;
}

.produce-card:hover {
  transform: translateY(-3px);
}

.produce-cover {
  height: 160px;
  margin-bottom: 0.8rem;
}

.produce-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.produce-info {
  margin-bottom: 0.8rem;
}

.produce-title {
  font-weight: bold;
  font-size: 0.9rem;
  line-height: 1.2;
}

.produce-manufacturer {
  color: #606266;
  margin-bottom: 0.3rem;
  font-size: 0.8rem;
}

.produce-price {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 0.3rem;
  font-size: 0.9rem;
}

.produce-stock {
  color: #67c23a;
  font-size: 0.8rem;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* ========== 响应式样式 ========== */
@media (max-width: 768px) {
  .recommend-section {
    padding: 0.75rem;
  }

  .recommend-list {
    grid-template-columns: repeat(3, 1fr);
    gap: 0.5rem;
  }

  .recommend-cover {
    height: 120px;
  }

  .search-bar {
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

  .filter-wrapper {
    width: 100%;
  }

  .filter-btn {
    width: 100%;
  }

  .filter-dropdown {
    width: 100%;
    left: 0;
    right: auto;
  }

  .produce-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }

  .produce-cover {
    height: 140px;
  }
}

@media (max-width: 480px) {
  .recommend-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .produce-list {
    grid-template-columns: 1fr;
    gap: 0.8rem;
  }
}
</style>