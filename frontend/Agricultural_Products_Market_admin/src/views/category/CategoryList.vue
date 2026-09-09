<template>
  <div class="category-list-container">
    <h2 class="page-title">分类管理</h2>
    <div class="search-bar">
      <el-input
        v-model="searchForm.categoryName"
        placeholder="请输入分类名称"
        style="width: 200px; margin-right: 10px"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="$router.push('/category/add')">添加分类</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <el-table :data="categoryList" style="width: 100%" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类名称" width="200" />
      <el-table-column prop="createTime" label="创建时间" width="200" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(scope.row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, deleteCategory } from '@/apis/category.js'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const categoryList = ref([])

const searchForm = reactive({
  categoryName: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const loading = ref(false)

const fetchCategoryList = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      let data = res.data || []
      // 如果是分页数据，提取records和total
      if (data.records) {
        categoryList.value = data.records
        pagination.total = data.total || 0
      } else {
        categoryList.value = data
        pagination.total = data.length || 0
      }
      // 应用搜索过滤
      if (searchForm.categoryName) {
        categoryList.value = categoryList.value.filter(category => 
          category.categoryName.includes(searchForm.categoryName)
        )
        pagination.total = categoryList.value.length
      }
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchCategoryList()
}

const handleReset = () => {
  searchForm.categoryName = ''
  pagination.currentPage = 1
  fetchCategoryList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchCategoryList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchCategoryList()
}

const handleEdit = (row) => {
  router.push(`/category/edit/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.categoryName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteCategory(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategoryList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个分类？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    for (const id of selectedIds.value) await deleteCategory(id)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchCategoryList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped>
.category-list-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-title {
  margin-bottom: 20px;
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
