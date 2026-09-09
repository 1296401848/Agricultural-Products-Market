<template>
  <div class="produce-list-container">
    <h2 class="page-title">农产品管理</h2>
    <div class="search-bar">
      <el-input
        v-model="searchForm.produceName"
        placeholder="请输入农产品名称"
        style="width: 200px; margin-right: 10px"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-select
        v-model="searchForm.categoryId"
        placeholder="请选择分类"
        style="width: 150px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option
          v-for="category in categoryList"
          :key="category.id"
          :label="category.categoryName"
          :value="category.id"
        />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="请选择状态"
        style="width: 150px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="上架" value="1" />
        <el-option label="下架" value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="$router.push('/produce/add')">添加农产品</el-button>
      <el-button v-if="selectedIds.length > 0" type="warning" @click="batchDown">批量下架</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <el-table ref="tableRef" :data="produceList" style="width: 100%" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="produceName" label="农产品名称" min-width="140" />
      <el-table-column label="分类" min-width="80">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
      <el-table-column prop="manufacturer" label="生产商" min-width="80" />
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column prop="status" label="状态" width="70">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="150" />
      <el-table-column label="操作" min-width="200">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="handleUpdateStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '下架' : '上架' }}
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
import { getProduceList, updateProduceStatus, deleteProduce, batchUpdateProduceStatus, batchDeleteProduces } from '@/apis/produce.js'
import { getCategoryList } from '@/apis/category.js'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const produceList = ref([])
const categoryList = ref([])

const searchForm = reactive({
  produceName: '',
  categoryId: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const loading = ref(false)

const fetchProduceList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      produceName: searchForm.produceName,
      categoryId: searchForm.categoryId,
      status: searchForm.status
    }
    const res = await getProduceList(params)
    if (res.code === 200) {
      produceList.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取农产品列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categoryList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchProduceList()
}

const handleReset = () => {
  searchForm.produceName = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchProduceList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchProduceList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchProduceList()
}

const handleEdit = (row) => {
  router.push(`/produce/edit/${row.id}`)
}

const handleUpdateStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '下架' : '上架'}农产品《${row.produceName}》吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await updateProduceStatus({
      id: row.id,
      status: row.status === 1 ? 0 : 1
    })
    if (res.code === 200) {
      ElMessage.success(`${row.status === 1 ? '下架' : '上架'}成功`)
      fetchProduceList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${row.status === 1 ? '下架' : '上架'}失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除农产品《${row.produceName}》吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deleteProduce(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchProduceList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 根据categoryId获取分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(cat => cat.id === categoryId)
  return category ? category.categoryName : '未知分类'
}

const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchDown = async () => {
  try {
    await ElMessageBox.confirm(`确定下架选中的 ${selectedIds.value.length} 件农产品？`, '提示', { type: 'warning' })
    await batchUpdateProduceStatus(selectedIds.value, 0)
    ElMessage.success('批量下架成功'); selectedIds.value = []; fetchProduceList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 件农产品？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    await batchDeleteProduces(selectedIds.value)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchProduceList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchCategoryList()
  fetchProduceList()
})
</script>

<style scoped>
.produce-list-container {
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
