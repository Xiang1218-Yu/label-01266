import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    if (res.code === 200) {
      const data = res.data
      token.value = data.token
      userId.value = data.userId
      username.value = data.username
      realName.value = data.realName

      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('realName', data.realName)

      return true
    }
    return false
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    clearUserInfo()
    router.push('/login')
  }

  function clearUserInfo() {
    token.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
  }

  return {
    token,
    userId,
    username,
    realName,
    isLoggedIn,
    login,
    logout,
    clearUserInfo
  }
})
