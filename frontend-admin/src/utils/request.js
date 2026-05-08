import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // blob 类型直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.clearUserInfo()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  async error => {
    let message = '网络错误，请稍后重试'
    const userStore = useUserStore()
    
    if (error.response) {
      let data = error.response.data
      
      // 处理 blob 类型的错误响应
      if (data instanceof Blob) {
        try {
          const text = await data.text()
          data = JSON.parse(text)
        } catch (e) {
          data = {}
        }
      }
      
      switch (error.response.status) {
        case 400:
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = data?.message || '登录已过期，请重新登录'
          userStore.clearUserInfo()
          router.push('/login')
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = data?.message || '服务器错误'
          break
      }
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
