import { reactive } from 'vue'

/** 农产品详情页加载后，存储当前农产品的分类信息，供面包屑使用 */
export const categoryCache = reactive({
  categoryName: '',
  categoryId: null
})
