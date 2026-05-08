<template>
  <div class="layout">
    <header class="header">
      <div class="header-left">
        <div class="logo">
          <el-icon :size="24"><HomeFilled /></el-icon>
          <span>公租房管理系统</span>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" class="avatar">
              {{ userStore.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="username">{{ userStore.realName || userStore.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <main class="main">
      <router-view v-slot="{ Component }">
        <keep-alive include="Building">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #fff;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 20px;
    transition: background 0.3s;
    outline: none;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
    
    &:focus {
      outline: none;
      background: rgba(255, 255, 255, 0.1);
    }
    
    .avatar {
      background: rgba(255, 255, 255, 0.2);
      color: #fff;
    }
    
    .username {
      font-size: 14px;
    }
  }
}

.main {
  flex: 1;
  background: #f5f7fa;
}
</style>
