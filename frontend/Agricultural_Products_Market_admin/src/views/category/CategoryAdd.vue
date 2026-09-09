<template>
  <div class="category-add-container">
    <h2 class="page-title">添加分类</h2>
    <el-form :model="categoryForm" :rules="rules" ref="categoryFormRef" label-width="100px" style="width: 400px;">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">提交</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addCategory } from '@/apis/category.js'

const router = useRouter()
const categoryFormRef = ref()
const loading = ref(false)

const categoryForm = reactive({
  categoryName: ''
})

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      addCategory(categoryForm)
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('分类添加成功')
            router.push('/category/list')
          }
        })
        .catch(err => {
          ElMessage.error('分类添加失败')
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

const handleReset = () => {
  if (!categoryFormRef.value) return
  categoryFormRef.value.resetFields()
}
</script>

<style scoped>
.category-add-container {
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
</style>
