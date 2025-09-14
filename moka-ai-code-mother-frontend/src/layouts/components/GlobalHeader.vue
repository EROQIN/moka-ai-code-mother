<template>
  <div class="global-header">
    <!-- 左侧 Logo 和标题 -->
    <div class="header-left">
      <div class="logo-wrapper">
        <img src="@/assets/logo.jpg" alt="Logo" class="logo" />
        <span class="site-title">Moka AI Code Mother</span>
      </div>
    </div>

    <!-- 中间菜单 -->
    <div class="header-center">
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        class="header-menu"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </div>

    <!-- 右侧用户信息 -->
    <div class="header-right">
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="doLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Button, message, type MenuProps } from 'ant-design-vue'
import type { MenuInfo } from 'ant-design-vue/es/menu/src/interface'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController.ts'

const AMenu = Menu
const AButton = Button

const router = useRouter()
const route = useRoute()

// 当前选中的菜单项
const selectedKeys = ref<string[]>([])

// 获取当前登录用户
const loginUserStore = useLoginUserStore()

// 根据当前路由更新选中状态
const updateSelectedKeys = () => {
  selectedKeys.value = [route.path]
}

// 监听路由变化
router.afterEach(() => {
  updateSelectedKeys()
})

// 初始化选中状态
updateSelectedKeys()

// 菜单点击处理
const handleMenuClick = (info: MenuInfo) => {
  router.push(info.key as string)
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
/// 菜单配置项
const originItems = [
  {
    key: '/',
    // icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 600px;
  margin: 0 24px;
}

.header-menu {
  border-bottom: none;
  background: transparent;
  min-width: 200px;
}

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .global-header {
    padding: 0 16px;
  }

  .header-center {
    margin: 0 16px;
  }

  .site-title {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 12px;
  }

  .header-center {
    margin: 0 12px;
    max-width: 300px;
  }

  .site-title {
    display: none;
  }

  .logo {
    width: 36px;
    height: 36px;
  }
}

@media (max-width: 576px) {
  .global-header {
    padding: 0 8px;
  }

  .header-center {
    margin: 0 8px;
  }

  .header-menu {
    min-width: 150px;
  }

  .header-menu :deep(.ant-menu-item) {
    padding: 0 8px;
  }
}
</style>
