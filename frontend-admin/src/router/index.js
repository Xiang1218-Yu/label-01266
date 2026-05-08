import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/building',
    children: [
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/Building.vue'),
        meta: { title: '楼盘表', requiresAuth: true }
      },
      {
        path: 'room/:id',
        name: 'RoomDetail',
        component: () => import('@/views/RoomDetail.vue'),
        meta: { title: '房屋详情', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.token) {
    next({ name: 'Building' })
  } else {
    next()
  }
})

export default router
