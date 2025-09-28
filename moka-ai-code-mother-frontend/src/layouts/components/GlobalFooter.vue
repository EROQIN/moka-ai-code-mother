<template>
  <div class="global-footer">
    <div class="footer-content">
      <div class="footer-info">
        <span class="author">© 2025 EROQIN</span>
        <a-divider type="vertical" />
        <a
          href="https://github.com/EROQIN/moka-ai-code-mother"
          target="_blank"
          rel="noopener noreferrer"
          class="github-link"
        >
          <GithubOutlined />
          GitHub 开源地址
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Divider } from 'ant-design-vue'
import { GithubOutlined } from '@ant-design/icons-vue'

const ADivider = Divider
</script>

<style scoped>
.global-footer {
  background: #ffffff;
  border-top: 1px solid #e8e8e800;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.footer-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.author {
  color: #666;
}

.github-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1890ff;
  text-decoration: none;
  transition: color 0.3s;
}

.github-link:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    padding: 16px;
  }

  .footer-info {
    flex-direction: column;
    gap: 12px;
  }

  .footer-info :deep(.ant-divider-vertical) {
    display: none;
  }
}

@media (max-width: 576px) {
  .footer-content {
    padding: 12px;
  }

  .footer-info {
    font-size: 12px;
  }

  .github-link {
    gap: 4px;
  }
}
</style>
