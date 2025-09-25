<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { marked } from 'marked'
import {
  UserOutlined,
  RobotOutlined,
  PaperClipOutlined,
  AudioOutlined,
  GlobalOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

// 获取应用ID
const appId = ref<string>(route.params.id as string)

// 响应式数据
const app = ref<API.AppVO>()
const loading = ref(false)
const deployLoading = ref(false)
const chatLoading = ref(false)
const messages = ref<Array<{ role: 'user' | 'assistant'; content: string; timestamp: number }>>([])
const currentMessage = ref('')
const showWebsite = ref(false)
const websiteUrl = ref('')

// 聊天容器引用
const chatContainer = ref<HTMLElement>()
const messageInput = ref<HTMLTextAreaElement>()

// 加载应用信息
const loadApp = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
      // 自动发送初始提示词
      if (app.value.initPrompt && messages.value.length === 0) {
        await sendMessage(app.value.initPrompt, false)
      }
    } else {
      message.error('应用不存在')
      router.push('/')
    }
  } catch (error) {
    message.error('加载应用失败')
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async (content: string, isUserInput = true) => {
  if (!content.trim()) return

  // 添加用户消息
  if (isUserInput) {
    messages.value.push({
      role: 'user',
      content: content.trim(),
      timestamp: Date.now(),
    })
  }

  // 清空输入框
  currentMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 开始AI回复
  chatLoading.value = true

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    timestamp: Date.now(),
  })

  try {
    // 创建SSE连接
    const eventSource = new EventSource(
      `http://localhost:8123/api/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(content)}`,
      { withCredentials: true },
    )

    eventSource.onmessage = (event) => {
      const data = event.data

      // 解析JSON格式的流式数据
      try {
        const jsonData = JSON.parse(data)
        if (jsonData.d) {
          // 提取文本内容并追加到消息
          messages.value[aiMessageIndex].content += jsonData.d
        }
      } catch (error) {
        // 如果不是JSON格式，直接追加原始数据
        messages.value[aiMessageIndex].content += data
      }
      scrollToBottom()
    }

    // 监听 done 事件
    eventSource.addEventListener('done', () => {
      eventSource.close()
      chatLoading.value = false
      // 显示网站预览
      showWebsite.value = true
      websiteUrl.value = `http://localhost:8123/api/static/${app.value?.codeGenType}_${appId.value}/`
    })

    eventSource.onerror = () => {
      eventSource.close()
      chatLoading.value = false
      message.error('生成失败，请重试')
    }
  } catch (error) {
    chatLoading.value = false
    message.error('发送消息失败')
  }
}

// 处理发送消息
const handleSendMessage = () => {
  if (currentMessage.value.trim() && !chatLoading.value) {
    sendMessage(currentMessage.value)
  }
}

// 处理键盘事件
const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSendMessage()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 部署应用
const handleDeploy = async () => {
  deployLoading.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0) {
      message.success('部署成功！')
      message.info(`访问地址：${res.data.data}`)
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    message.error('部署失败')
  } finally {
    deployLoading.value = false
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// Markdown 渲染函数
const renderMarkdown = (content: string) => {
  if (!content) return ''

  // 配置 marked 选项
  marked.setOptions({
    breaks: true, // 支持换行
    gfm: true, // 支持 GitHub Flavored Markdown
  })

  return marked(content)
}

// 页面加载时获取应用信息
onMounted(() => {
  loadApp()
})
</script>

<template>
  <div class="app-chat-page">
    <a-spin :spinning="loading" class="page-loading">
      <!-- 顶部栏 -->
      <div class="top-bar">
        <div class="top-bar-left">
          <a-button type="text" @click="goHome" class="back-btn"> ← 返回 </a-button>
          <h1 class="app-title">{{ app?.appName || '应用对话' }}</h1>
        </div>
        <div class="top-bar-right">
          <a-button
            type="primary"
            :loading="deployLoading"
            @click="handleDeploy"
            :disabled="!showWebsite"
          >
            部署按钮
          </a-button>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 对话区域 -->
        <div class="chat-section">
          <!-- 消息区域 -->
          <div ref="chatContainer" class="messages-container">
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['message', message.role]"
            >
              <div class="message-avatar">
                <UserOutlined v-if="message.role === 'user'" />
                <RobotOutlined v-else />
              </div>
              <div class="message-content">
                <div
                  class="message-text"
                  v-if="message.role === 'assistant'"
                  v-html="renderMarkdown(message.content)"
                ></div>
                <div class="message-text" v-else>{{ message.content }}</div>
                <div class="message-time">
                  {{ new Date(message.timestamp).toLocaleTimeString() }}
                </div>
              </div>
            </div>

            <!-- 加载指示器 -->
            <div v-if="chatLoading" class="message assistant">
              <div class="message-avatar">
                <RobotOutlined />
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-section">
            <div class="input-wrapper">
              <a-textarea
                ref="messageInput"
                v-model:value="currentMessage"
                placeholder="请描述您想要的功能或修改..."
                :rows="3"
                :disabled="chatLoading"
                @keydown="handleKeyDown"
                class="message-input"
              />
              <div class="input-actions">
                <div class="input-tools">
                  <a-button type="text" size="small">
                    <template #icon>
                      <PaperClipOutlined />
                    </template>
                    上传
                  </a-button>
                  <a-button type="text" size="small">
                    <template #icon>
                      <AudioOutlined />
                    </template>
                    语音
                  </a-button>
                </div>
                <a-button
                  type="primary"
                  :loading="chatLoading"
                  :disabled="!currentMessage.trim()"
                  @click="handleSendMessage"
                  class="send-btn"
                >
                  发送
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 网页预览区域 -->
        <div class="preview-section">
          <div class="preview-header">
            <h3>生成后的网页展示</h3>
            <div v-if="showWebsite" class="preview-url">
              <a :href="websiteUrl" target="_blank" class="url-link">
                {{ websiteUrl }}
              </a>
            </div>
          </div>

          <div class="preview-content">
            <div v-if="!showWebsite" class="preview-placeholder">
              <div class="placeholder-icon">
                <GlobalOutlined />
              </div>
              <p>网站生成完成后将在此处展示</p>
            </div>
            <iframe v-else :src="websiteUrl" class="preview-iframe" frameborder="0"></iframe>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.app-chat-page {
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.page-loading {
  height: 100%;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #666;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #e8e8e8;
}

/* 对话区域 */
.chat-section {
  height: 75vh;
  display: flex;
  flex-direction: column;
  background: white;
  min-height: 500px;
  max-height: 85vh;
}

.messages-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #1890ff;
  color: white;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown 样式 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 16px 0 8px 0;
  font-weight: 600;
  line-height: 1.3;
}

.message-text :deep(h1) {
  font-size: 1.5em;
}
.message-text :deep(h2) {
  font-size: 1.3em;
}
.message-text :deep(h3) {
  font-size: 1.1em;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text :deep(li) {
  margin: 4px 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9em;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text :deep(pre code) {
  background: none;
  padding: 0;
}

.message-text :deep(blockquote) {
  border-left: 4px solid #ddd;
  margin: 8px 0;
  padding-left: 16px;
  color: #666;
}

.message-text :deep(strong) {
  font-weight: 600;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.message-text :deep(a:hover) {
  text-decoration: underline;
}

.message.user .message-text {
  background: #1890ff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 输入区域 */
.input-section {
  padding: 10px;
  border-top: 1px solid #e8e8e8;
  background: white;
}

.input-wrapper {
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  overflow: hidden;
}

.message-input {
  border: none;
  box-shadow: none;
  resize: none;
}

.message-input:focus {
  box-shadow: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
}

.input-tools {
  display: flex;
  gap: 8px;
}

/* 网页预览区域 */
.preview-section {
  display: flex;
  flex-direction: column;
  /* background: rgb(255, 255, 255); */
  height: 80vh;
  min-height: 400px;
  border: 3px solid rgb(240, 240, 240);
  /* border-radius: 16px; */
}

.preview-header {
  padding: 16px 24px;
  border-bottom: 2px solid #e8e8e8;
  background: white;
}

.preview-header h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-url {
  font-size: 12px;
}

.url-link {
  color: #1859ff;
  font-size: medium;
  text-decoration: none;
}

.url-link:hover {
  text-decoration: underline;
}

.preview-content {
  flex: 1;
  position: relative;
}

.preview-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .chat-section {
    height: 55vh;
    min-height: 400px;
  }

  .preview-section {
    min-height: 300px;
  }
}

@media (max-width: 768px) {
  .top-bar {
    padding: 12px 16px;
  }

  .top-bar-left {
    gap: 12px;
  }

  .app-title {
    font-size: 16px;
  }

  .messages-container {
    padding: 16px;
  }

  .input-section {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .input-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .input-tools {
    justify-content: center;
  }
}
</style>
