<template>
  <div class="produce-edit-container">
    <h2 class="page-title">编辑农产品</h2>
    <el-form :model="produceForm" :rules="rules" ref="produceFormRef" label-width="100px" style="width: 600px;">
      <el-form-item label="农产品名称" prop="produceName">
        <el-input v-model="produceForm.produceName" placeholder="请输入农产品名称" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="produceForm.categoryId" placeholder="请选择分类">
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.categoryName"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="生产商" prop="manufacturer">
        <el-input v-model="produceForm.manufacturer" placeholder="请输入生产商" />
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="produceForm.price" :min="0.01" :precision="2" placeholder="请输入价格" />
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number v-model="produceForm.stock" :min="0" :step="1" placeholder="请输入库存" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="produceForm.status">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="封面URL" prop="coverUrl">
        <el-input v-model="produceForm.coverUrl" placeholder="请输入封面图片URL" />
      </el-form-item>
      <el-form-item label="农产品描述" prop="description">
        <el-input
          v-model="produceForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入农产品描述"
        />
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
import { updateProduce, getProduceDetail } from '@/apis/produce.js'
import { getCategoryList } from '@/apis/category.js'

const router = useRouter()
const route = useRoute()
const produceFormRef = ref()
const loading = ref(false)
const categoryList = ref([])
const produceId = ref(route.params.id)

const produceForm = reactive({
  produceName: '',
  categoryId: '',
  manufacturer: '',
  price: 0,
  stock: 0,
  status: 1,
  coverUrl: '',
  description: ''
})

const rules = {
  produceName: [
    { required: true, message: '请输入农产品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'blur' }
  ],
  manufacturer: [
    { required: true, message: '请输入生产商', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格不能小于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' }
  ],
  coverUrl: [
    { required: true, message: '请输入封面URL', trigger: 'blur' }
  ]
}

const fetchProduceDetail = async () => {
  loading.value = true
  try {
    const res = await getProduceDetail(produceId.value)
    if (res.code === 200) {
      Object.assign(produceForm, res.data)
    }

  } catch (error) {
    ElMessage.error('获取农产品详情失败')
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

const handleSubmit = async () => {
  if (!produceFormRef.value) return
  await produceFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      updateProduce(produceId.value, produceForm)
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('农产品更新成功')
            router.push('/produce/list')
          }
        })
        .catch(err => {
          ElMessage.error('农产品更新失败')
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

const handleReset = () => {
  if (!produceFormRef.value) return
  produceFormRef.value.resetFields()
}

// 监听路由参数变化，重新获取农产品详情
watch(() => route.params.id, (newId) => {
  produceId.value = newId
  fetchProduceDetail()
})

onMounted(() => {
  fetchCategoryList()
  fetchProduceDetail()
})
</script>

<style scoped>
.produce-edit-container {
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
