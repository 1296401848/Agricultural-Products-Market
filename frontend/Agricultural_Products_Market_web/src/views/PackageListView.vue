<template>
  <div class="package-page">
    <h2 class="page-title">套餐专区</h2>
    <div class="package-grid">
      <el-card v-for="pkg in list" :key="pkg.id" shadow="hover" class="package-card" @click="goDetail(pkg.id)">
        <div class="pkg-cover">
          <el-image :src="pkg.coverImg || 'https://via.placeholder.com/280x180'" fit="cover" />
        </div>
        <div class="pkg-info">
          <div class="pkg-name">{{ pkg.packageName }}</div>
          <div class="pkg-desc">{{ pkg.packageDesc }}</div>
          <div class="pkg-prices">
            <span class="original">¥{{ pkg.originalPrice }}</span>
            <span class="price">¥{{ pkg.packagePrice }}</span>
            <span class="sales">已售{{ pkg.sales || 0 }}</span>
          </div>
        </div>
      </el-card>
      <el-empty v-if="list.length === 0" description="暂无套餐" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPackageList } from '@/apis/package.js'

const router = useRouter()
const list = ref([])

onMounted(async () => {
  const res = await getPackageList()
  if (res.code === 200) list.value = res.data
})

const goDetail = (id) => router.push(`/package/${id}`)
</script>

<style scoped>
.package-page { max-width: 1200px; margin: 0 auto; padding: 0 1rem; }
.page-title { font-size: 1.5rem; margin-bottom: 1.5rem; color: #303133; }
.package-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.25rem; }
.package-card { cursor: pointer; transition: transform 0.3s; overflow: hidden; }
.package-card:hover { transform: translateY(-4px); }
.pkg-cover { height: 180px; overflow: hidden; }
.pkg-cover .el-image { width: 100%; height: 100%; }
.pkg-info { padding: 0.75rem 0.5rem; display: flex; flex-direction: column; gap: 0.35rem; }
.pkg-name { font-weight: bold; font-size: 1rem; }
.pkg-desc { color: #909399; font-size: 0.85rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.original { text-decoration: line-through; color: #c0c4cc; font-size: 0.85rem; margin-right: 0.5rem; }
.price { color: #f56c6c; font-weight: bold; font-size: 1.2rem; }
.sales { color: #909399; font-size: 0.8rem; float: right; }

@media (max-width: 768px) { .package-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .package-grid { grid-template-columns: 1fr; } }
</style>
