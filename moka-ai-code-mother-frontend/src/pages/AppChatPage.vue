<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, deployApp, deleteApp } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import request from '@/request'
import {
  UserOutlined,
  RobotOutlined,
  PaperClipOutlined,
  AudioOutlined,
  GlobalOutlined,
  InfoCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  UploadOutlined,
  CheckCircleOutlined,
  CopyOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 获取应用ID
const appId = ref<string>(route.params.id as string)

// 响应式数据
const app = ref<API.AppVO>()
const loading = ref(false)
const deployLoading = ref(false)
const chatLoading = ref(false)
const messages = ref<
  Array<{ role: 'user' | 'assistant'; content: string; timestamp: number; id?: number }>
>([])
const currentMessage = ref('')
const showWebsite = ref(false)
const websiteUrl = ref('')
const showAppDetail = ref(false)
const deleteLoading = ref(false)
const showDeploySuccess = ref(false)
const deployedUrl = ref('')

// 对话历史相关
const chatHistoryLoading = ref(false)
const hasMoreHistory = ref(true)
const lastCreateTime = ref<string>()

// 聊天容器引用
const chatContainer = ref<HTMLElement>()
const messageInput = ref<HTMLTextAreaElement>()

// 预览iframe引用
const previewIframe = ref<HTMLIFrameElement>()

// 刷新预览相关
const refreshKey = ref(0) // 用于强制刷新iframe的key

// 检查是否有编辑权限
const canEdit = computed(() => {
  if (!app.value || !loginUserStore.loginUser) return false
  return (
    app.value.userId === loginUserStore.loginUser.id ||
    loginUserStore.loginUser.userRole === 'admin'
  )
})

// 加载对话历史
const loadChatHistory = async () => {
  if (chatHistoryLoading.value || !hasMoreHistory.value) return

  chatHistoryLoading.value = true
  try {
    const res = await listAppChatHistory({
      appId: appId.value,
      pageSize: 10,
      lastCreateTime: lastCreateTime.value,
    })

    if (res.data.code === 0 && res.data.data) {
      const historyData = res.data.data
      const historyRecords = historyData.records || []

      // 转换历史记录为消息格式
      const newMessages = historyRecords.map((record) => {
        // 根据消息类型判断角色
        const role = record.messageType === 'user' ? 'user' : 'assistant'
        return {
          role,
          content: record.message || '',
          timestamp: new Date(record.createTime || '').getTime(),
          id: record.id,
        }
      })

      // 如果是第一页，直接替换消息列表并按时间戳排序
      if (!lastCreateTime.value) {
        messages.value = newMessages.sort((a, b) => a.timestamp - b.timestamp)
      } else {
        // 如果是加载更多，追加到消息列表末尾并按时间戳排序
        messages.value = [...messages.value, ...newMessages].sort((a, b) => a.timestamp - b.timestamp)
      }

      // 更新游标和是否有更多数据
      if (historyRecords.length > 0) {
        lastCreateTime.value = historyRecords[historyRecords.length - 1].createTime
      }

      hasMoreHistory.value = historyData.totalRow > messages.value.length

      // 如果有至少2条对话记录，显示网站预览
      if (messages.value.length >= 2) {
        showWebsite.value = true
        const baseURL = request.defaults.baseURL || 'http://localhost:8123/api'
        websiteUrl.value = `${baseURL}/static/${app.value?.codeGenType}_${appId.value}/`
      }
    }
  } catch (error) {
    message.error('加载对话历史失败')
  } finally {
    chatHistoryLoading.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory()
}

// 加载应用信息
const loadApp = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data

      // 先加载对话历史
      await loadChatHistory()

      // 修改自动发送初始消息的逻辑：移除view参数，只有自己的app且没有对话历史时才自动发送
      const isMyApp = app.value.userId === loginUserStore.loginUser?.id
      const hasHistory = messages.value.length > 0

      if (app.value.initPrompt && !hasHistory && isMyApp) {
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

  // 添加用户消息（无论是否来自用户输入，都显示用户消息）
  messages.value.push({
    role: 'user',
    content: content.trim(),
    timestamp: Date.now(),
  })

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
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || 'http://localhost:8123/api'

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: content,
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    const eventSource = new EventSource(url, {
      withCredentials: true,
    })

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

      // 修改网站展示逻辑：如果有至少1条对话记录才显示网站预览
      if (messages.value.length >= 1) {
        showWebsite.value = true
        const baseURL = request.defaults.baseURL || 'http://localhost:8123/api'
        websiteUrl.value = `${baseURL}/static/${app.value?.codeGenType}_${appId.value}/`
        
        // AI修改页面结束后，自动刷新预览页面
        setTimeout(() => {
          refreshPreview()
        }, 1000) // 延迟1秒刷新，确保页面已生成
      }
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
  if (event.key === 'Enter' && event.ctrlKey) {
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
      deployedUrl.value = res.data.data
      showDeploySuccess.value = true
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    message.error('部署失败')
  } finally {
    deployLoading.value = false
  }
}

// 复制部署地址
const copyDeployedUrl = async () => {
  try {
    await navigator.clipboard.writeText(deployedUrl.value)
    message.success('地址已复制到剪贴板')
  } catch (error) {
    message.error('复制失败')
  }
}

// 访问网站
const visitWebsite = () => {
  window.open(deployedUrl.value, '_blank')
}

// 关闭部署成功弹窗
const closeDeploySuccess = () => {
  showDeploySuccess.value = false
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 显示应用详情
const showAppDetailModal = () => {
  showAppDetail.value = true
}

// 关闭应用详情
const closeAppDetail = () => {
  showAppDetail.value = false
}

// 编辑应用
const editApp = () => {
  router.push(`/app/edit/${appId.value}`)
}

// 删除应用
const handleDeleteApp = () => {
  message.confirm({
    title: '确认删除',
    content: '确定要删除这个应用吗？删除后无法恢复。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      deleteLoading.value = true
      try {
        const res = await deleteApp({ id: appId.value })
        if (res.data.code === 0) {
          message.success('删除成功')
          router.push('/')
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败')
      } finally {
        deleteLoading.value = false
      }
    },
  })
}

// 检查是否有操作权限（本人或管理员）
const hasOperationPermission = () => {
  if (!app.value || !loginUserStore.loginUser) return false
  return (
    app.value.userId === loginUserStore.loginUser.id ||
    loginUserStore.loginUser.userRole === 'admin'
  )
}

// 刷新预览页面
const refreshPreview = () => {
  // 通过改变key来强制重新加载iframe
  refreshKey.value += 1
  message.success('预览页面已刷新')
}

// 初始化 markdown-it 实例
const md = new MarkdownIt({
  html: true, // 允许 HTML 标签
  xhtmlOut: false, // 使用 HTML 而不是 XHTML
  breaks: false, // 不自动将单个换行转换为 <br>，需要两个换行才会分段
  linkify: true, // 自动识别链接
  typographer: true, // 启用一些语言中性的替换 + 引号美化
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch (__) {}
    }

    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

// Markdown 渲染函数
const renderMarkdown = (content: string) => {
  if (!content) return ''

  try {
    // 预处理文本：规范化换行
    //将两个以上的换行符替换成一个换行符
    const processedContent = content
    return md.render(processedContent)
  } catch (error) {
    console.error('Markdown 渲染错误:', error)
    return content
  }
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
          <a-space>
            <a-button @click="showAppDetailModal">
              <template #icon>
                <InfoCircleOutlined />
              </template>
              应用详情
            </a-button>
            <a-button
              type="primary"
              :loading="deployLoading"
              @click="handleDeploy"
              :disabled="!showWebsite"
            >
              <template #icon>
                <UploadOutlined />
              </template>
              部署应用
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 对话区域 -->
        <div class="chat-section">
          <!-- 消息区域 -->
          <div ref="chatContainer" class="messages-container">
            <!-- 加载更多历史消息按钮 -->
            <div v-if="hasMoreHistory" class="load-more-container">
              <a-button
                type="link"
                :loading="chatHistoryLoading"
                @click="loadMoreHistory"
                class="load-more-btn"
              >
                <template #icon>
                  <ReloadOutlined />
                </template>
                加载更多历史消息
              </a-button>
            </div>

            <div
              v-for="(message, index) in messages"
              :key="message.id || index"
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
              <a-tooltip v-if="!canEdit" title="无法在别人的作品下对话哦~" placement="top">
                <a-textarea
                  ref="messageInput"
                  v-model:value="currentMessage"
                  placeholder="无法在别人的作品下对话哦~"
                  :rows="3"
                  :disabled="true"
                  class="message-input disabled-input"
                />
              </a-tooltip>
              <a-textarea
                v-else
                ref="messageInput"
                v-model:value="currentMessage"
                placeholder="请描述您想要的功能或修改... (Ctrl+Enter 发送)"
                :rows="3"
                :disabled="chatLoading"
                @keydown="handleKeyDown"
                class="message-input"
              />
              <div class="input-actions">
                <div class="input-tools">
                  <a-button type="text" size="small" :disabled="!canEdit">
                    <template #icon>
                      <PaperClipOutlined />
                    </template>
                    上传
                  </a-button>
                  <a-button type="text" size="small" :disabled="!canEdit">
                    <template #icon>
                      <AudioOutlined />
                    </template>
                    语音
                  </a-button>
                </div>
                <a-button
                  type="primary"
                  :loading="chatLoading"
                  :disabled="!canEdit || !currentMessage.trim()"
                  @click="handleSendMessage"
                  class="send-btn"
                >
                  发送
                </a-button>
              </div>
              <div v-if="canEdit" class="input-hint">
                <span class="hint-text">提示：按 Ctrl+Enter 快速发送消息</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 网页预览区域 -->
        <div class="preview-section">
          <div class="preview-header">
            <h3>页面预览：</h3>
            <div v-if="showWebsite" class="preview-header-actions">
              <div class="preview-url">
                <a :href="websiteUrl" target="_blank" class="url-link">
                  {{ websiteUrl }}
                </a>
              </div>
              <a-button type="primary" size="small" @click="refreshPreview" class="refresh-btn">
                <template #icon>
                  <ReloadOutlined />
                </template>
                刷新预览
              </a-button>
            </div>
          </div>

          <div class="preview-content">
            <div v-if="!showWebsite" class="preview-placeholder">
              <div class="placeholder-icon">
                <GlobalOutlined />
              </div>
              <p>网站生成完成后将在此处展示</p>
            </div>
            <iframe 
              v-else 
              :key="refreshKey"
              :src="websiteUrl" 
              class="preview-iframe" 
              frameborder="0"
              ref="previewIframe"
            ></iframe>
          </div>
        </div>
      </div>

      <!-- 应用详情弹窗 -->
      <a-modal
        v-model:open="showAppDetail"
        title="应用详情"
        :footer="null"
        width="500px"
        @cancel="closeAppDetail"
      >
        <div v-if="app" class="app-detail-content">
          <!-- 应用基础信息 -->
          <div class="detail-section">
            <h4>应用基础信息</h4>
            <div class="info-item">
              <span class="label">应用名称：</span>
              <span class="value">{{ app.appName }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建者：</span>
              <div class="creator-info">
                <a-avatar size="small">
                  <template #icon>
                    <UserOutlined />
                  </template>
                </a-avatar>
                <span class="creator-name">用户ID: {{ app.userId || '未知' }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ app.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">代码类型：</span>
              <a-tag
                :color="
                  app.codeGenType === 'HTML'
                    ? 'blue'
                    : app.codeGenType === 'MULTI_FILE'
                      ? 'green'
                      : 'orange'
                "
              >
                {{ app.codeGenType }}
              </a-tag>
            </div>
            <div v-if="app.initPrompt" class="info-item">
              <span class="label">初始提示词：</span>
              <div class="prompt-text">{{ app.initPrompt }}</div>
            </div>
          </div>

          <!-- 操作栏（仅本人或管理员可见） -->
          <div v-if="hasOperationPermission()" class="detail-section">
            <h4>操作</h4>
            <a-space>
              <a-button type="primary" @click="editApp">
                <template #icon>
                  <EditOutlined />
                </template>
                修改
              </a-button>
              <a-button danger :loading="deleteLoading" @click="handleDeleteApp">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-space>
          </div>
        </div>
      </a-modal>

      <!-- 部署成功弹窗 -->
      <a-modal
        v-model:open="showDeploySuccess"
        title="部署成功"
        :footer="null"
        width="400px"
        @cancel="closeDeploySuccess"
      >
        <div class="deploy-success-content">
          <!-- 成功图标 -->
          <div class="success-icon">
            <CheckCircleOutlined style="font-size: 48px; color: #52c41a" />
          </div>

          <!-- 成功标题 -->
          <h3 class="success-title">网站部署成功！</h3>

          <!-- 提示文字 -->
          <p class="success-description">你的网站已经成功部署，可以通过以下链接访问：</p>

          <!-- 网址展示和复制 -->
          <div class="url-section">
            <div class="url-display">
              <span class="url-text">{{ deployedUrl }}</span>
              <a-button type="text" size="small" @click="copyDeployedUrl" class="copy-btn">
                <template #icon>
                  <CopyOutlined />
                </template>
              </a-button>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <a-button type="primary" @click="visitWebsite"> 访问网站 </a-button>
            <a-button @click="closeDeploySuccess"> 关闭 </a-button>
          </div>
        </div>
      </a-modal>
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

/* 加载更多按钮样式 */
.load-more-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.load-more-btn {
  color: #1890ff;
  font-size: 14px;
}

.load-more-btn:hover {
  color: #40a9ff;
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
  flex: 0 1 auto;
  max-width: 70%;
  min-width: min-content;
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
  color: inherit;
}

.message-text :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 8px;
}
.message-text :deep(h2) {
  font-size: 1.3em;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding-bottom: 4px;
}
.message-text :deep(h3) {
  font-size: 1.1em;
}

.message-text :deep(p) {
  margin: 8px 0;
  line-height: 1.6;
}

/* 列表样式优化 */
.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
  line-height: 1.6;
}

.message-text :deep(ul) {
  list-style-type: disc;
}

.message-text :deep(ol) {
  list-style-type: decimal;
}

.message-text :deep(li) {
  margin: 6px 0;
  padding-left: 4px;
}

.message-text :deep(li > ul),
.message-text :deep(li > ol) {
  margin: 4px 0;
  padding-left: 20px;
}

.message-text :deep(li > p) {
  margin: 4px 0;
}

/* 嵌套列表样式 */
.message-text :deep(ul ul) {
  list-style-type: circle;
}

.message-text :deep(ul ul ul) {
  list-style-type: square;
}

/* 代码样式 */
.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 0.9em;
  color: #e83e8c;
}

/* Highlight.js 代码块样式 */
.message-text :deep(pre.hljs) {
  background: #f8f8f8;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 0.9em;
  line-height: 1.5;
}

.message-text :deep(pre.hljs code) {
  background: none;
  padding: 0;
  color: inherit;
  font-size: inherit;
  border-radius: 0;
}

/* 用户消息中的代码块样式调整 */
.message.user .message-text :deep(pre.hljs) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.message.user .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

/* 普通 pre 标签样式（兼容性） */
.message-text :deep(pre:not(.hljs)) {
  background: rgba(0, 0, 0, 0.05);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.message-text :deep(pre:not(.hljs) code) {
  background: none;
  padding: 0;
  color: inherit;
  font-size: 0.9em;
}

/* 引用样式 */
.message-text :deep(blockquote) {
  border-left: 4px solid #ddd;
  margin: 12px 0;
  padding: 8px 16px;
  color: #666;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 0 4px 4px 0;
}

.message-text :deep(blockquote p) {
  margin: 4px 0;
}

/* 文本格式 */
.message-text :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.message-text :deep(em) {
  font-style: italic;
}

.message-text :deep(del) {
  text-decoration: line-through;
  opacity: 0.7;
}

/* 链接样式 */
.message-text :deep(a) {
  color: #1890ff;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.message-text :deep(a:hover) {
  border-bottom-color: #1890ff;
}

/* 表格样式 */
.message-text :deep(table) {
  border-collapse: collapse;
  margin: 12px 0;
  width: 100%;
  font-size: 0.9em;
}

.message-text :deep(th),
.message-text :deep(td) {
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 8px 12px;
  text-align: left;
}

.message-text :deep(th) {
  background: rgba(0, 0, 0, 0.05);
  font-weight: 600;
}

/* 分隔线 */
.message-text :deep(hr) {
  border: none;
  border-top: 2px solid rgba(0, 0, 0, 0.1);
  margin: 20px 0;
}

/* 换行处理 */
.message-text :deep(br) {
  line-height: 1.6;
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

.disabled-input {
  background-color: #f5f5f5 !important;
  cursor: not-allowed !important;
}

.disabled-input:hover {
  border-color: #d9d9d9 !important;
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

.input-hint {
  padding: 8px 16px;
  background: #f0f7ff;
  border-top: 1px solid #d6e4ff;
}

.hint-text {
  font-size: 12px;
  color: #1890ff;
  display: flex;
  align-items: center;
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

.preview-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.preview-url {
  font-size: 12px;
  flex: 1;
}

.refresh-btn {
  flex-shrink: 0;
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

/* 应用详情弹窗样式 */
.app-detail-content {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  line-height: 1.5;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  min-width: 80px;
  color: #6b7280;
  font-weight: 500;
  flex-shrink: 0;
}

.info-item .value {
  color: #1f2937;
  flex: 1;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.creator-name {
  color: #1f2937;
  font-weight: 500;
}

.prompt-text {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  color: #495057;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 120px;
  overflow-y: auto;
  flex: 1;
}

/* 部署成功弹窗样式 */
.deploy-success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 16px;
}

.success-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.success-description {
  color: #6b7280;
  margin: 0 0 20px 0;
  font-size: 14px;
}

.url-section {
  margin-bottom: 24px;
}

.url-display {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 8px 12px;
  gap: 8px;
}

.url-text {
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #495057;
  word-break: break-all;
}

.copy-btn {
  flex-shrink: 0;
  color: #1890ff;
}

.copy-btn:hover {
  background: #e6f7ff;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.action-buttons .ant-btn {
  min-width: 80px;
}
</style>
