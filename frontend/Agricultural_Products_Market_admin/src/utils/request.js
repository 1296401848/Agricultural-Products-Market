// 请求的实例
import axios from 'axios';
import {ElMessage} from 'element-plus'
import {useTokenStore} from '@/stores/token.js'
import {useUserStore} from "@/stores/user.js";

//定义一个变量,记录公共的前缀  ,  baseURL
const baseURL = 'http://localhost:8080/admin';

const instance = axios.create({baseURL})


// 请求拦截器：
instance.interceptors.request.use(
    (config) => {
        // 对于OPTIONS预检请求，不添加Authorization头，并且确保不触发预检
        if (config.method !== 'OPTIONS') {
            const tokenStore = useTokenStore()
            const token = tokenStore.token
            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`; // 自动添加请求头
                console.log('请求携带token:', token.substring(0, 20) + '...') // 调试日志
            } else {
                console.warn('警告：请求时没有token') // 调试日志
            }
            // 注意：不在这里做token检查和跳转，由路由守卫统一处理
        } else {
            // 对于OPTIONS请求，移除可能引起问题的头部
            delete config.headers['Authorization'];
            // 设置预检请求的特殊处理
            config.timeout = 10000; // 增加预检请求的超时时间
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截器：统一处理结果
instance.interceptors.response.use(
    (response) => {
        if (response.data.code === 200) {
            if (response.msg != null){
                ElMessage.success(response.msg)
            }
            return response.data;
        } else if (response.data.code === 401) {
            ElMessage.error('请重新登录');
            const tokenStore = useTokenStore();
            const userStore = useUserStore();
            tokenStore.removeToken();
            userStore.removeUserInfo();
            window.location.href = '/login';
            return Promise.reject(new Error('未授权'));
        } else {
            ElMessage.error(response.data.msg || '系统错误')
            return Promise.reject(new Error(response.data.msg || '系统错误'));
        }
    }, // 直接返回响应体的 data
    (error) => {
        ElMessage.error(error.response?.data?.msg || '请求失败');
        // 刷新 Token 失败时跳转登录
        if (error.response?.status === 401) {
            const tokenStore = useTokenStore();
            const userStore = useUserStore();
            tokenStore.removeToken();
            userStore.removeUserInfo();
            window.location.href = '/login';
        } else {
            ElMessage.error('服务异常')
        }

        return Promise.reject(error);
    }
);


export default instance;
