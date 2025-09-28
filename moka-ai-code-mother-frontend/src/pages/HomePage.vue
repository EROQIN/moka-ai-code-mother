<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage } from '@/api/appController'
import {
  CodeTwoTone,
  PaperClipOutlined,
  ThunderboltOutlined,
  RocketOutlined,
  RobotOutlined,
  MessageOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()

// 响应式数据
const promptInput = ref('')
const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const featuredAppsLoading = ref(false)
const createLoading = ref(false)

// 分页参数
const myAppsPagination = ref({
  current: 1,
  pageSize: 8,
  total: 0,
})

const featuredAppsPagination = ref({
  current: 1,
  pageSize: 8,
  total: 0,
})

// 搜索参数
const myAppsSearchName = ref('')
const featuredAppsSearchName = ref('')

// 创建应用
const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.error('请输入应用描述')
    return
  }

  createLoading.value = true
  try {
    const res = await addApp({
      appName: '新应用',
      initPrompt: promptInput.value,
      codeGenType: 'HTML',
    })

    if (res.data.code === 0) {
      message.success('应用创建成功')
      // 跳转到对话页面
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    message.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

// 加载我的应用
const loadMyApps = async (page = 1, searchName = '') => {
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      pageNum: page,
      pageSize: myAppsPagination.value.pageSize,
      appName: searchName,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      // 临时调试：输出应用数据查看 deployKey 字段
      console.log('我的应用数据:', myApps.value)
      myAppsPagination.value = {
        current: res.data.data.pageNumber || 1,
        pageSize: res.data.data.pageSize || 8,
        total: res.data.data.totalRow || 0,
      }
    }
  } catch (error) {
    message.error('加载我的应用失败')
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用
const loadFeaturedApps = async (page = 1, searchName = '') => {
  featuredAppsLoading.value = true
  try {
    const res = await listFeaturedAppVoByPage({
      pageNum: page,
      pageSize: featuredAppsPagination.value.pageSize,
      appName: searchName,
      sortField: 'priority',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPagination.value = {
        current: res.data.data.pageNumber || 1,
        pageSize: res.data.data.pageSize || 20,
        total: res.data.data.totalRow || 0,
      }
    }
  } catch (error) {
    message.error('加载精选应用失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

// 我的应用分页变化
const handleMyAppsPageChange = (page: number) => {
  loadMyApps(page, myAppsSearchName.value)
}

// 精选应用分页变化
const handleFeaturedAppsPageChange = (page: number) => {
  loadFeaturedApps(page, featuredAppsSearchName.value)
}

// 我的应用搜索
const handleMyAppsSearch = () => {
  loadMyApps(1, myAppsSearchName.value)
}

// 精选应用搜索
const handleFeaturedAppsSearch = () => {
  loadFeaturedApps(1, featuredAppsSearchName.value)
}

// 查看应用详情（查看对话）
const viewApp = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}?view=1`)
}

// 查看对话
const viewChat = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}?view=1`)
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    window.open(`http://localhost/${app.deployKey}`, '_blank')
  } else {
    message.warning('该应用尚未部署')
  }
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

// 快速创建模板
const quickTemplates = [
  '创建一个个人博客网站，包含文章列表、详情页面、分类标签功能，支持响应式设计，风格简洁现代，配色以蓝白为主，包含导航栏、侧边栏和底部版权信息',
  '制作一个在线简历展示网站，包含个人信息、工作经历、技能展示、项目作品等模块，采用单页面设计，支持打印功能，整体风格专业大气',
  '开发一个任务管理工具网页，具备添加、编辑、删除任务功能，支持任务分类、优先级设置、完成状态标记，界面简洁易用，适合日常工作使用',
  '构建一个产品展示网站，包含产品列表、详情介绍、图片轮播、联系方式等功能，采用卡片式布局，支持移动端适配，整体设计现代时尚',
]

const useTemplate = (template: string) => {
  promptInput.value = template
}

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
})
</script>

<template>
  <div class="home-page">
    <!-- 网站标题和描述 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">AI 应用生成平台</h1>
        <p class="hero-subtitle">一句话轻松创建网站应用</p>
      </div>
    </div>

    <!-- 用户提示词输入区域 -->
    <div class="input-section">
      <div class="input-wrapper">
        <a-textarea
          v-model:value="promptInput"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          class="prompt-input"
          @keydown.ctrl.enter="handleCreateApp"
        />
        <div class="input-actions">
          <div class="quick-templates">
            <a-button
              v-for="(template, index) in quickTemplates"
              :key="index"
              size="small"
              type="text"
              @click="useTemplate(template)"
              class="template-btn"
            >
              {{
                index === 0
                  ? '个人博客'
                  : index === 1
                    ? '在线简历'
                    : index === 2
                      ? '任务管理'
                      : '产品展示'
              }}
            </a-button>
          </div>
          <div class="submit-actions">
            <a-button
              type="primary"
              :loading="createLoading"
              @click="handleCreateApp"
              class="create-btn"
            >
              创建应用
              <template #icon>
                <RocketOutlined />
              </template>
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的应用列表 -->
    <div class="apps-section">
      <div class="section-header">
        <h2 class="section-title">我的作品</h2>
        <div class="section-actions">
          <a-input-search
            v-model:value="myAppsSearchName"
            placeholder="搜索我的应用"
            style="width: 200px"
            @search="handleMyAppsSearch"
          />
        </div>
      </div>

      <a-spin :spinning="myAppsLoading">
        <div class="apps-grid">
          <div v-for="app in myApps" :key="app.id" class="app-card" @click="viewApp(app)">
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
              <div v-else class="cover-placeholder">
                <RobotOutlined />
              </div>
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.appName }}</h3>
              <p class="app-time">创建于 {{ app.createTime }}</p>
              <div class="app-actions">
                <a-button type="text" size="small" @click.stop="viewChat(app)">
                  <template #icon>
                    <MessageOutlined />
                  </template>
                  查看对话
                </a-button>
                <a-button v-if="app.deployKey" type="text" size="small" @click.stop="viewWork(app)">
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看作品
                </a-button>
                <!-- 临时调试：显示所有应用的查看作品按钮 -->
                <a-button
                  v-if="!app.deployKey"
                  type="text"
                  size="small"
                  @click.stop="viewWork(app)"
                  style="opacity: 0.5"
                >
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看作品(无部署)
                </a-button>
                <a-button type="text" size="small" @click.stop="editApp(app)"> 编辑 </a-button>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <div v-if="myAppsPagination.total > 0" class="pagination-wrapper">
        <a-pagination
          v-model:current="myAppsPagination.current"
          :total="myAppsPagination.total"
          :page-size="myAppsPagination.pageSize"
          :show-size-changer="false"
          @change="handleMyAppsPageChange"
        />
      </div>
    </div>

    <!-- 精选应用列表 -->
    <div class="apps-section">
      <div class="section-header">
        <h2 class="section-title">精选案例</h2>
        <div class="section-actions">
          <a-input-search
            v-model:value="featuredAppsSearchName"
            placeholder="搜索精选应用"
            style="width: 200px"
            @search="handleFeaturedAppsSearch"
          />
        </div>
      </div>

      <a-spin :spinning="featuredAppsLoading">
        <div class="apps-grid">
          <div
            v-for="app in featuredApps"
            :key="app.id"
            class="app-card featured"
            @click="viewApp(app)"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="cover-image" />
              <div v-else class="cover-placeholder">
                <RobotOutlined />
              </div>
              <div class="featured-badge">精选</div>
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.appName }}</h3>
              <p class="app-desc">{{ app.initPrompt?.substring(0, 50) }}...</p>
              <div class="app-meta">
                <span class="app-type">{{ app.codeGenType }}</span>
                <span class="app-author">NoCode 官方</span>
              </div>
              <div class="app-actions">
                <a-button type="text" size="small" @click.stop="viewChat(app)">
                  <template #icon>
                    <MessageOutlined />
                  </template>
                  查看对话
                </a-button>
                <a-button v-if="app.deployKey" type="text" size="small" @click.stop="viewWork(app)">
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看作品
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <div v-if="featuredAppsPagination.total > 0" class="pagination-wrapper">
        <a-pagination
          v-model:current="featuredAppsPagination.current"
          :total="featuredAppsPagination.total"
          :page-size="featuredAppsPagination.pageSize"
          :show-size-changer="false"
          @change="handleFeaturedAppsPageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  width: 100%;
  margin: 0;
  padding: 0;
  background: transparent;
  min-height: 100vh;
}

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 80px 24px 60px;
  color: #333;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #333;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.3);
}

.hero-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
}

/* 输入区域 */
.input-section {
  margin-bottom: 48px;
  padding: 0 24px;
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.prompt-input {
  border: none;
  box-shadow: none;
  font-size: 16px;
  padding: 20px 24px 16px;
  resize: none;
}

.prompt-input:focus {
  box-shadow: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fafafa;
}

.quick-templates {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.template-btn {
  border-radius: 20px;
  background: #f0f0f0;
  border: 1px solid #d9d9d9;
}

.template-btn:hover {
  background: #e6f7ff;
  border-color: #1890ff;
  color: #1890ff;
}

.submit-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-btn {
  border-radius: 8px;
  height: 40px;
  padding: 0 20px;
  font-weight: 500;
}

/* 应用列表区域 */
.apps-section {
  margin-bottom: 48px;
  padding: 0 24px;
  background: white;
  border-radius: 16px 16px 0 0;
  padding-top: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.app-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.app-card.featured {
  border: 2px solid #1890ff;
}

.app-cover {
  position: relative;
  height: 180px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 48px;
  color: #d9d9d9;
}

.featured-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #1890ff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.app-info {
  padding: 16px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1f2937;
}

.app-time {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
}

.app-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.app-type {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  color: #666;
}

.app-author {
  color: #1890ff;
  font-weight: 500;
}

.app-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    padding: 60px 16px 40px;
  }

  .hero-title {
    font-size: 32px;
  }

  .input-section {
    padding: 0 16px;
  }

  .input-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .quick-templates {
    justify-content: center;
  }

  .submit-actions {
    justify-content: center;
  }

  .apps-section {
    padding: 32px 16px 48px;
  }

  .apps-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
}

@media (max-width: 576px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .prompt-input {
    padding: 16px;
    font-size: 14px;
  }

  .input-actions {
    padding: 12px 16px;
  }
}
</style>
