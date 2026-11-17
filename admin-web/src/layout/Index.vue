<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <div class="logo">
        <span class="logo-icon">
          <svg viewBox="0 0 32 32" width="20" height="20" aria-hidden="true">
            <path d="M4 17h24a12 12 0 0 1-24 0z" fill="currentColor" />
          </svg>
        </span>
        <span v-show="!isCollapse" class="logo-text">苍穹外卖</span>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#0a1f34"
        text-color="#c3d2e4"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item
          v-for="item in menuList"
          :key="item.path"
          :index="item.path"
        >
          <i :class="item.icon"></i>
          <span slot="title">{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'" class="collapse-btn" @click="isCollapse = !isCollapse"></i>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <i class="el-icon-user-solid"></i>
              {{ userName }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import request, { removeToken } from '@/utils/request'

export default {
  name: 'Layout',
  data() {
    return {
      isCollapse: false,
      menuList: [
        { path: '/dashboard', title: '工作台', icon: 'el-icon-monitor' },
        { path: '/category', title: '分类管理', icon: 'el-icon-menu' },
        { path: '/dish', title: '菜品管理', icon: 'el-icon-goods' },
        { path: '/setmeal', title: '套餐管理', icon: 'el-icon-box' },
        { path: '/order', title: '订单管理', icon: 'el-icon-document' },
        { path: '/employee', title: '员工管理', icon: 'el-icon-user' },
        { path: '/statistics', title: '数据统计', icon: 'el-icon-data-line' }
      ]
    }
  },
  computed: {
    currentTitle() {
      return this.$route.meta.title || ''
    },
    userName() {
      try {
        const u = JSON.parse(localStorage.getItem('sky_admin_user') || '{}')
        return u.name || u.userName || '员工'
      } catch (e) {
        return '员工'
      }
    }
  },
  methods: {
    async handleCommand(cmd) {
      if (cmd === 'logout') {
        try {
          await request('/admin/employee/logout', { method: 'POST' })
        } catch (e) {
          // 忽略退出接口异常
        }
        removeToken()
        localStorage.removeItem('sky_admin_user')
        this.$router.push('/login')
        this.$message.success('已退出登录')
      }
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.sidebar {
  background: #0a1f34;
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(140deg, #3d8bff, #1f6feb);
  flex: none;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar .el-menu {
  border-right: none;
}

.sidebar .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(31, 111, 235, 0.95), rgba(31, 111, 235, 0.45)) !important;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-info {
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.user-info:hover {
  color: #409eff;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
