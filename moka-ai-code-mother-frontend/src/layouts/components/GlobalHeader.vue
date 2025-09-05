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
      <a-button type="primary" @click="handleLogin"> 登录 </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Menu, Button } from 'ant-design-vue'
import type { MenuInfo } from 'ant-design-vue/es/menu/src/interface'

const AMenu = Menu
const AButton = Button

const router = useRouter()
const route = useRoute()

// 当前选中的菜单项
const selectedKeys = ref<string[]>([])

// 菜单配置
const menuItems = computed(() => [
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
])

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

// 登录按钮处理
const handleLogin = () => {
  console.log('登录功能待实现')
}
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
