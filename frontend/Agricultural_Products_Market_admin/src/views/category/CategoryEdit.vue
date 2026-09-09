<template>
  <div class="category-edit-container">
    <h2 class="page-title">编辑分类</h2>
    <el-form :model="categoryForm" :rules="rules" ref="categoryFormRef" label-width="100px" style="width: 400px;">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateCategory, getCategoryList } from '@/apis/category.js'

const router = useRouter()
const route = useRoute()
const categoryFormRef = ref()
const loading = ref(false)
const categoryId = ref(route.params.id)

const categoryForm = reactive({
  categoryName: ''
})

const rules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const fetchCategoryDetail = async () => {
  loading.value = true
  try {
    // 由于没有单独的获取分类详情API，我们从分类列表中查找
    const res = await getCategoryList()
    if (res.code === 200) {
      const categories = res.data || []
      const category = categories.find(cat => cat.id === parseInt(categoryId.value))
      if (category) {
        categoryForm.categoryName = category.categoryName
      } else {
        ElMessage.error('分类不存在')
        router.push('/category/list')
      }
    }
  } catch (error) {
    ElMessage.error('获取分类详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!categoryFormRef.value) return
  await categoryFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      updateCategory(categoryId.value, categoryForm)
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('分类更新成功')
            router.push('/category/list')
          }
        })
        .catch(err => {
          ElMessage.error('分类更新失败')
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

// 监听路由参数变化，重新获取分类详情
watch(() => route.params.id, (newId) => {
  categoryId.value = newId
  fetchCategoryDetail()
})

onMounted(() => {
  fetchCategoryDetail()
})
</script>

<style scoped>
.category-edit-container {
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
