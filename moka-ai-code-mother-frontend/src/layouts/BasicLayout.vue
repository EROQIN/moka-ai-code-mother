<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="header">
      <GlobalHeader />
    </a-layout-header>

    <!-- 中间内容区 -->
    <a-layout-content class="content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>

    <!-- 底部页脚 -->
    <a-layout-footer class="footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { Layout } from 'ant-design-vue'
import { RouterView } from 'vue-router'
import GlobalHeader from './components/GlobalHeader.vue'
import GlobalFooter from './components/GlobalFooter.vue'

const ALayout = Layout
const ALayoutHeader = Layout.Header
const ALayoutContent = Layout.Content
const ALayoutFooter = Layout.Footer
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.content {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 200px);
}

.footer {
  padding: 0;
  background: #fff;
  border-top: 1px solid #e8e8e8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }

  .content-wrapper {
    padding: 16px;
    border-radius: 0;
    box-shadow: none;
  }
}

@media (max-width: 576px) {
  .content {
    padding: 8px;
  }

  .content-wrapper {
    padding: 12px;
  }
}
</style>
