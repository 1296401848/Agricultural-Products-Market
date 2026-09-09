<template>
  <div class="order-list-container">
    <h2 class="page-title">订单管理</h2>
    <div class="search-bar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 300px; margin-right: 10px"
      />
      <el-select
        v-model="searchForm.payStatus"
        placeholder="请选择支付状态"
        style="width: 150px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="未支付" value="0" />
        <el-option label="已支付" value="1" />
      </el-select>
      <el-select
        v-model="searchForm.orderStatus"
        placeholder="请选择订单状态"
        style="width: 150px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="待发货" value="待发货" />
        <el-option label="已发货" value="已发货" />
        <el-option label="已完成" value="已完成" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button v-if="selectedIds.length > 0" type="warning" @click="batchUpdateStatus('已完成')">批量完成</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <el-table :data="orderList" style="width: 100%" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单编号" min-width="150" />
      <el-table-column prop="userId" label="用户ID" width="70" />
      <el-table-column label="金额" min-width="150">
        <template #default="{ row }">
          <div>实付：¥{{ (row.payAmount || row.totalAmount || 0).toFixed(2) }}</div>
          <div v-if="row.couponDiscount > 0" style="font-size: 0.8rem; color: #67c23a">优惠：-¥{{ row.couponDiscount.toFixed(2) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="payStatus" label="支付状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="订单状态" min-width="80">
        <template #default="scope">
          <el-tag :type="getOrderStatusType(scope.row.orderStatus)">
            {{ scope.row.orderStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="150" />
      <el-table-column prop="payTime" label="支付时间" width="200" />
      <el-table-column label="操作" min-width="200">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleViewDetail(scope.row)"
          >
            查看详情
          </el-button>
          <el-button
            type="warning"
            size="small"
            @click="handleUpdateStatus(scope.row)"
            :disabled="scope.row.orderStatus === '已完成'"
          >
            修改状态
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="订单详情"
      width="80%"
    >
      <div v-if="currentOrder" class="order-detail">
        <h3>订单信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderInfo.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentOrder.orderInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ (currentOrder.orderInfo.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.orderInfo.couponDiscount > 0" label="优惠券抵扣">-¥{{ currentOrder.orderInfo.couponDiscount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ (currentOrder.orderInfo.payAmount || currentOrder.orderInfo.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="currentOrder.orderInfo.payStatus === 1 ? 'success' : 'danger'">
              {{ currentOrder.orderInfo.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(currentOrder.orderInfo.orderStatus)">
              {{ currentOrder.orderInfo.orderStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.orderInfo.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrder.orderInfo.payTime || '未支付' }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px">订单项</h3>
        <el-table :data="currentOrder.orderItems" style="width: 100%" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="类型" width="70">
            <template #default="{ row }">
              <el-tag v-if="row.itemType === 2" type="warning" size="small">套餐</el-tag>
              <el-tag v-else type="info" size="small">普通</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="produceName" label="农产品名称" width="200" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="createTime" label="创建时间" width="200" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 订单状态更新对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="修改订单状态"
      width="50%"
    >
      <el-form :model="statusForm" :rules="statusRules" ref="statusFormRef" label-width="80px">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="statusForm.orderNo" readonly />
        </el-form-item>
        <el-form-item label="当前状态" prop="currentStatus">
          <el-input v-model="statusForm.currentStatus" readonly />
        </el-form-item>
        <el-form-item label="新状态" prop="newStatus">
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态">
            <el-option label="待发货" value="待发货" />
            <el-option label="已发货" value="已发货" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmStatus">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, updateOrderStatus, getOrderDetail, batchUpdateOrderStatus, batchDeleteOrders } from '@/apis/order.js'

const orderList = ref([])
const dateRange = ref([])
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const currentOrder = ref(null)
const statusFormRef = ref(null)

const searchForm = reactive({
  payStatus: '',
  orderStatus: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const statusForm = reactive({
  orderNo: '',
  currentStatus: '',
  newStatus: '',
  orderId: ''
})

const statusRules = {
  newStatus: [
    { required: true, message: '请选择新状态', trigger: 'blur' }
  ]
}

const loading = ref(false)

const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      startDate: dateRange.value[0] ? dateRange.value[0] : '',
      endDate: dateRange.value[1] ? dateRange.value[1] : '',
      payStatus: searchForm.payStatus,
      orderStatus: searchForm.orderStatus
    }
    const res = await getOrderList(params)
    if (res.code === 200) {
      orderList.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrderList()
}

const handleReset = () => {
  searchForm.payStatus = ''
  searchForm.orderStatus = ''
  dateRange.value = []
  pagination.currentPage = 1
  fetchOrderList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchOrderList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchOrderList()
}

const handleViewDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      console.log(currentOrder.value)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

const handleUpdateStatus = (row) => {
  statusForm.orderNo = row.orderNo
  statusForm.currentStatus = row.orderStatus
  statusForm.newStatus = ''
  statusForm.orderId = row.id
  statusDialogVisible.value = true
}

const handleConfirmStatus = async () => {
  if (!statusFormRef.value) return
  await statusFormRef.value.validate((valid) => {
    if (valid) {
      updateOrderStatus({
        id: statusForm.orderId,
        orderStatus: statusForm.newStatus
      })
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('订单状态更新成功')
            statusDialogVisible.value = false
            fetchOrderList()
          }
        })
        .catch(err => {
          ElMessage.error('订单状态更新失败')
        })
    }
  })
}

const getOrderStatusType = (status) => {
  switch (status) {
    case '待发货':
      return 'warning'
    case '已发货':
      return 'info'
    case '已完成':
      return 'success'
    default:
      return 'primary'
  }
}

const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchUpdateStatus = async (status) => {
  try {
    await ElMessageBox.confirm(`确定将选中的 ${selectedIds.value.length} 个订单改为「${status}」？`, '提示', { type: 'warning' })
    await batchUpdateOrderStatus(selectedIds.value, status)
    ElMessage.success('批量操作成功'); selectedIds.value = []; fetchOrderList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个订单？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    await batchDeleteOrders(selectedIds.value)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchOrderList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.order-list-container {
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
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-detail {
  max-height: 600px;
  overflow-y: auto;
}

.dialog-footer {
  text-align: right;
}
</style>
