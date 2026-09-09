//定义用户信息store
import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useUserStore = defineStore('user', () => {
  //1.响应式变量
  const userInfo = ref({
    id: null,
    username: '',
    role: '',
    status: 1
  })

  //2.定义一个函数,修改用户信息
  const setUserInfo = (newUserInfo) => {
    userInfo.value = newUserInfo
  }

  //3.函数,移除用户信息
  const removeUserInfo = () => {
    userInfo.value = {
      id: null,
      username: '',
      role: '',
      status: 1,
      createTime: ''
    }
  }

  return {
    userInfo, setUserInfo, removeUserInfo
  }
}, {
  persist: true//持久化存储
})