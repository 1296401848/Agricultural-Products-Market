<template>
  <div class="container">
    <div class="search-bar">
      <el-input v-model="searchName" placeholder="搜索套餐名" style="width: 220px" clearable @keyup.enter="fetchList" />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button type="success" @click="showAdd">创建套餐</el-button>
      <el-button v-if="selectedIds.length > 0" type="warning" @click="batchDown">批量下架</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <el-table :data="list" stripe style="width: 100%; margin-top: 1rem" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="packageName" label="名称" min-width="140" />
      <el-table-column label="原价/套餐价" width="150">
        <template #default="{ row }">
          <span style="text-decoration:line-through;color:#909399">¥{{ row.originalPrice }}</span>
          &nbsp;<span style="color:#f56c6c;font-weight:bold">¥{{ row.packagePrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="推荐" width="80">
        <template #default="{ row }">{{ row.isRecommend ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggle(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="size" layout="total, prev, pager, next" :total="total" @current-change="fetchList" style="margin-top:1rem;justify-content:flex-end" />

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dlg" :title="editingId ? '编辑套餐' : '创建套餐'" width="650px" append-to-body>
      <el-form :model="form" label-width="80px">
        <el-form-item label="套餐名称" required><el-input v-model="form.packageName" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.packageDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverImg" placeholder="https://..." /></el-form-item>
        <el-form-item label="套餐价格" required><el-input v-model="form.packagePrice" /></el-form-item>
        <el-form-item label="是否推荐"><el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="关联农产品" required>
          <div class="produce-item-row">
            <div class="produce-item-list">
              <div v-for="(b, i) in form.produces" :key="i" class="produce-item">
                <span class="produce-item-name">{{ b.produceName }}</span>
                <span class="produce-item-label">数量</span>
                <el-input-number v-model="b.produceNum" :min="1" size="small" style="width:110px" />
                <el-button @click="form.produces.splice(i,1)" type="danger" size="small" :icon="Delete" circle />
              </div>
            </div>
            <el-button @click="openProduceSelector" size="small" class="add-produce-btn">+ 添加农产品</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 选择农产品弹窗 -->
    <el-dialog v-model="addProduceDlg" title="选择农产品" width="700px" append-to-body>
      <el-input v-model="produceSearch" placeholder="搜索农产品" style="margin-bottom:0.5rem" />
      <el-table :data="filteredProduces" stripe @row-click="selectProduce" :row-class-name="produceRowClass" style="cursor:pointer" height="350">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="produceName" label="名称" />
        <el-table-column prop="price" label="价格" width="80" />
        <el-table-column prop="stock" label="库存" width="80" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPackageList, createPackage, updatePackage, updatePackageStatus, batchUpdatePackageStatus, batchDeletePackages } from '@/apis/package.js'
import { getProduceList } from '@/apis/produce.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'
import { Delete } from '@element-plus/icons-vue'

const list = ref([]), page = ref(1), size = ref(10), total = ref(0), searchName = ref('')
const dlg = ref(false), editingId = ref(null)
const form = ref({ packageName: '', packageDesc: '', coverImg: '', packagePrice: '', isRecommend: 0, produces: [] })
const addProduceDlg = ref(false), produceList = ref([]), produceSearch = ref('')

const fetchList = async () => {
  const res = await getPackageList({ page: page.value, size: size.value, name: searchName.value })
  if (res.code === 200) { list.value = res.data.records; total.value = res.data.total }
}
const fetchProduces = async () => {
  const res = await getProduceList({ size: 200, page: 1 })
  if (res.code === 200) produceList.value = res.data.records || []
}
const openProduceSelector = () => { addProduceDlg.value = true; produceSearch.value = ''; fetchProduces() }
const filteredProduces = computed(() => {
  if (!produceSearch.value) return produceList.value
  const kw = produceSearch.value.toLowerCase()
  return produceList.value.filter(b => b.produceName.toLowerCase().includes(kw))
})
const showAdd = () => { editingId.value = null; form.value = { packageName: '', packageDesc: '', coverImg: '', packagePrice: '', isRecommend: 0, produces: [] }; dlg.value = true }
const showEdit = async (row) => {
  editingId.value = row.id
  form.value = { ...row, produces: [] }
  dlg.value = true
  try {
    const res = await request.get(`/admin/package/${row.id}`)
    if (res.code === 200 && res.data?.produces) {
      form.value.produces = res.data.produces.map(b => ({ produceId: b.produceId, produceName: b.produceName, produceNum: b.produceNum || 1 }))
    }
  } catch (e) { /* ignore */ }
}
const isProduceSelected = (produceId) => form.value.produces.some(b => b.produceId === produceId)
const produceRowClass = ({ row }) => isProduceSelected(row.id) ? 'selected-row' : ''
const selectProduce = (produce) => {
  if (isProduceSelected(produce.id)) return
  form.value.produces.push({ produceId: produce.id, produceName: produce.produceName, produceNum: 1 })
}
const handleSubmit = async () => {
  if (editingId.value) { await updatePackage(editingId.value, form.value) }
  else { await createPackage(form.value) }
  dlg.value = false; fetchList()
}
const toggle = async (row) => {
  await updatePackageStatus(row.id, row.status === 1 ? 0 : 1)
  fetchList()
}
const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchDown = async () => {
  try {
    await ElMessageBox.confirm(`确定下架选中的 ${selectedIds.value.length} 个套餐？`, '提示', { type: 'warning' })
    await batchUpdatePackageStatus(selectedIds.value, 0)
    ElMessage.success('批量下架成功'); selectedIds.value = []; fetchList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个套餐？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    await batchDeletePackages(selectedIds.value)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => fetchList())
</script>

<style scoped>
.container { padding: 0; }
.search-bar { display: flex; gap: 0.5rem; align-items: center; }

.produce-item-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.produce-item-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.produce-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.4rem 0.75rem;
  background: #f5f7fa;
  border-radius: 6px;
}

.produce-item-name {
  flex: 1;
  font-weight: bold;
  color: #303133;
}

.produce-item-label {
  color: #909399;
  font-size: 0.85rem;
}

.add-produce-btn {
  align-self: flex-start;
}

:deep(.selected-row) {
  background-color: #ECF7EB !important;
}
:deep(.selected-row td) {
  background-color: #ECF7EB !important;
}
</style>
