<template>
  <div class="coupon-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchName" placeholder="搜索优惠券名称" style="width: 240px" clearable @keyup.enter="fetchList" />
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button type="success" @click="showAddDialog">创建优惠券</el-button>
      <el-button v-if="selectedIds.length > 0" type="warning" @click="batchDown">批量下架</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" stripe style="width: 100%; margin-top: 1rem" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="couponName" label="名称" min-width="150" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ typeMap[row.couponType] }}</template>
      </el-table-column>
      <el-table-column label="面额" width="150">
        <template #default="{ row }">
          <span v-if="row.couponType === 3">打{{ (row.discount * 10).toFixed(0) }}折</span>
          <span v-else>减¥{{ row.reduceMoney }}</span>
          <span v-if="row.couponType === 1">（满¥{{ row.fullMoney }}）</span>
          <span v-else-if="row.couponType === 2">（无门槛）</span>
        </template>
      </el-table-column>
      <el-table-column label="领取" width="100">
        <template #default="{ row }">{{ row.usedCount }}/{{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="200">
        <template #default="{ row }">
          {{ row.startTime }} ~ {{ row.endTime }}<br/>
          <span style="color: #909399; font-size: 0.85rem">领取后{{ row.validDays }}天有效</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page" :page-size="size"
      layout="total, prev, pager, next" :total="total"
      @current-change="fetchList" style="margin-top: 1rem; justify-content: flex-end"
    />

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券' : '创建优惠券'" width="520px" append-to-body>
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="优惠券名称" required>
          <el-input v-model="form.couponName" placeholder="如：满100减20" />
        </el-form-item>
        <el-form-item label="优惠券类型" required>
          <el-radio-group v-model="form.couponType">
            <el-radio :value="1">满减券</el-radio>
            <el-radio :value="2">无门槛券</el-radio>
            <el-radio :value="3">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.couponType === 1" label="满减门槛">
          <el-input v-model="form.fullMoney" placeholder="满多少元可用" />
        </el-form-item>
        <el-form-item v-if="form.couponType !== 3" label="减免金额">
          <el-input v-model="form.reduceMoney" placeholder="减多少元" />
        </el-form-item>
        <el-form-item v-if="form.couponType === 3" label="折扣">
          <el-input v-model="form.discount" placeholder="如0.8代表8折" />
        </el-form-item>
        <el-form-item label="发放数量" required>
          <el-input-number v-model="form.totalCount" :min="1" />
        </el-form-item>
        <el-form-item label="领取时间">
          <el-date-picker v-model="timeRange" type="datetimerange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" :teleported="false" />
        </el-form-item>
        <el-form-item label="有效天数" required>
          <el-input-number v-model="form.validDays" :min="1" :max="365" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCouponList, createCoupon, updateCoupon, updateCouponStatus, batchUpdateCouponStatus, batchDeleteCoupons } from '@/apis/coupon.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchName = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const timeRange = ref([])

const typeMap = { 1: '满减券', 2: '无门槛券', 3: '折扣券' }

const form = ref({
  couponName: '', couponType: 1, fullMoney: null, reduceMoney: null,
  discount: null, totalCount: 100, validDays: 30, status: 1
})

const fetchList = async () => {
  const res = await getCouponList({ page: page.value, size: size.value, couponName: searchName.value })
  if (res.code === 200) {
    list.value = res.data.records
    total.value = res.data.total
  }
}

const showAddDialog = () => {
  editingId.value = null
  form.value = { couponName: '', couponType: 1, fullMoney: null, reduceMoney: null, discount: null, totalCount: 100, validDays: 30, status: 1 }
  timeRange.value = []
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  timeRange.value = row.startTime && row.endTime ? [row.startTime, row.endTime] : []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const data = { ...form.value }
  if (timeRange.value?.length === 2) {
    data.startTime = timeRange.value[0]
    data.endTime = timeRange.value[1]
  }
  if (editingId.value) {
    await updateCoupon(editingId.value, data)
  } else {
    await createCoupon(data)
  }
  dialogVisible.value = false
  fetchList()
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateCouponStatus(row.id, newStatus)
  fetchList()
}

const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchDown = async () => {
  try {
    await ElMessageBox.confirm(`确定下架选中的 ${selectedIds.value.length} 张优惠券？`, '提示', { type: 'warning' })
    await batchUpdateCouponStatus(selectedIds.value, 0)
    ElMessage.success('批量下架成功'); selectedIds.value = []; fetchList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 张优惠券？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    await batchDeleteCoupons(selectedIds.value)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => fetchList())
</script>

<style scoped>
.coupon-container { padding: 0; }
.search-bar { display: flex; gap: 0.5rem; align-items: center; }
</style>
