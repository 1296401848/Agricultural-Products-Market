<template>
  <div class="user-list-container">
    <h2 class="page-title">用户管理</h2>
    <div class="search-bar">
      <el-input
        v-model="searchForm.username"
        placeholder="请输入用户关键字"
        style="width: 200px; margin-right: 10px"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-select
        v-model="searchForm.status"
        placeholder="请选择状态"
        style="width: 150px; margin-right: 10px"
      >
        <el-option label="全部" value="" />
        <el-option label="活跃" value="1" />
        <el-option label="禁用" value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" @click="showAddDialog">添加用户</el-button>
      <el-button v-if="selectedIds.length > 0" type="warning" @click="batchDisable">批量禁用</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="batchDelete">批量删除 ({{ selectedIds.length }})</el-button>
    </div>

    <el-table :data="userList" style="width: 100%" stripe @selection-change="handleSelectionChange" @sort-change="handleSortChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" min-width="120" align="center" sortable="custom" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '活跃' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="150" sortable="custom" />
      <el-table-column label="操作" min-width="160">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="handleUpdateStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 添加用户弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加用户" width="450px" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="ROLE_USER">普通用户</el-radio>
            <el-radio value="ROLE_ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleAdd">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus, addUser, deleteUser, batchUpdateUserStatus, batchDeleteUsers } from '@/apis/user.js'
import { Search } from '@element-plus/icons-vue'

const userList = ref([])

const searchForm = reactive({
  username: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const sortParams = reactive({
  sortField: undefined,
  sortOrder: undefined
})

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '', email: '', role: 'ROLE_USER' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { pattern: /^(?=.*[a-zA-Z])(?=.*\d).{8,20}$/, message: '密码需包含英文字符和数字，长度8-20位', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}
const showAddDialog = () => { Object.assign(form, { username: '', password: '', email: '', role: 'ROLE_USER' }); dialogVisible.value = true }
const handleAdd = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return
    try { await addUser({ ...form }); ElMessage.success('添加成功'); dialogVisible.value = false; fetchUserList() } catch (e) { ElMessage.error(e?.response?.data?.msg || '添加失败') }
  })
}

const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      username: searchForm.username || undefined,
      status: searchForm.status || undefined,
      sortField: sortParams.sortField,
      sortOrder: sortParams.sortOrder
    }
    console.log('请求参数:', params)
    const res = await getUserList(params)
    if (res.code === 200) {
      userList.value = res.data.records || res.data
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.status = ''
  sortParams.sortField = undefined
  sortParams.sortOrder = undefined
  pagination.currentPage = 1
  fetchUserList()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchUserList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchUserList()
}

const handleSortChange = (sortInfo) => {
  console.log('排序变更:', sortInfo)
  if (sortInfo.order) {
    sortParams.sortField = sortInfo.prop
    sortParams.sortOrder = sortInfo.order === 'ascending' ? 'asc' : 'desc'
  } else {
    sortParams.sortField = undefined
    sortParams.sortOrder = undefined
  }
  pagination.currentPage = 1
  fetchUserList()
}

const handleUpdateStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}用户${row.username}吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await updateUserStatus({
      id: row.id,
      status: row.status === 1 ? 0 : 1
    })
    if (res.code === 200) {
      ElMessage.success(`${row.status === 1 ? '禁用' : '启用'}成功`)
      fetchUserList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${row.status === 1 ? '禁用' : '启用'}失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户${row.username}吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.msg || '删除失败')
    }
  }
}

const selectedIds = ref([])
const handleSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
const batchDisable = async () => {
  try {
    await ElMessageBox.confirm(`确定禁用选中的 ${selectedIds.value.length} 个用户？`, '提示', { type: 'warning' })
    await batchUpdateUserStatus(selectedIds.value, 0)
    ElMessage.success('批量禁用成功'); selectedIds.value = []; fetchUserList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户？此操作不可恢复！`, '警告', { confirmButtonText: '确定删除', type: 'error' })
    await batchDeleteUsers(selectedIds.value)
    ElMessage.success('批量删除成功'); selectedIds.value = []; fetchUserList()
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-list-container {
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
