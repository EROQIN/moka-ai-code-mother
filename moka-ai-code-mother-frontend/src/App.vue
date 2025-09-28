<script setup lang="ts">
import BasicLayout from './layouts/BasicLayout.vue'
import '@/access'

import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background-image: linear-gradient(to top, #fad0c4 0%, #fad0c4 1%, #ffd1ff 100%);
  background-attachment: fixed;
  background-repeat: no-repeat;
  background-size: cover;
}

#app {
  height: 100%;
}

/* 链接样式 */
a {
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

a:hover {
  color: #40a9ff;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
