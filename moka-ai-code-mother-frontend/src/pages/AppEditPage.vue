<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, getAppById, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 获取应用ID
const appId = ref<string>(route.params.id as string)

// 响应式数据
const app = ref<API.AppVO>()
const loading = ref(false)
const submitting = ref(false)
const isAdmin = ref(false)

// 表单数据
const form = ref({
  appName: '',
  cover: '',
  priority: 0,
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在 1 到 50 个字符', trigger: 'blur' },
  ],
}

// 加载应用信息
const loadApp = async () => {
  loading.value = true
  try {
    // 检查是否为管理员
    isAdmin.value = loginUserStore.loginUser.userRole === 'admin'

    let res
    if (isAdmin.value) {
      // 管理员可以获取完整信息
      res = await getAppById({ id: appId.value })
    } else {
      // 普通用户只能获取基本信息
      res = await getAppVoById({ id: appId.value })
    }

    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data

      // 检查权限：普通用户只能编辑自己的应用
      if (!isAdmin.value && app.value.userId !== loginUserStore.loginUser.id) {
        message.error('您没有权限编辑此应用')
        router.push('/')
        return
      }

      // 填充表单
      form.value = {
        appName: app.value.appName || '',
        cover: app.value.cover || '',
        priority: app.value.priority || 0,
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

// 提交表单
const handleSubmit = async () => {
  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员可以更新所有字段
      res = await updateAppByAdmin({
        id: appId.value,
        appName: form.value.appName,
        cover: form.value.cover,
        priority: form.value.priority,
      })
    } else {
      // 普通用户只能更新应用名称
      res = await updateApp({
        id: appId.value,
        appName: form.value.appName,
      })
    }

    if (res.data.code === 0) {
      message.success('更新成功')
      router.back()
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新失败')
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.back()
}

// 上传封面
const handleCoverUpload = (info: any) => {
  // 这里可以实现文件上传逻辑
  // 暂时使用模拟URL
  if (info.file.status === 'done') {
    form.value.cover = 'https://example.com/cover.jpg'
    message.success('封面上传成功')
  } else if (info.file.status === 'error') {
    message.error('封面上传失败')
  }
}

// 页面加载时获取应用信息
onMounted(() => {
  loadApp()
})
</script>

<template>
  <div class="app-edit-page">
    <a-spin :spinning="loading" class="page-loading">
      <div class="page-header">
        <div class="header-left">
          <a-button type="text" @click="handleCancel" class="back-btn"> ← 返回 </a-button>
          <div class="header-info">
            <h1>编辑应用</h1>
            <p v-if="app">{{ app.appName }}</p>
          </div>
        </div>
      </div>

      <div class="form-container">
        <a-form
          :model="form"
          :rules="rules"
          layout="vertical"
          @finish="handleSubmit"
          class="edit-form"
        >
          <!-- 应用名称 -->
          <a-form-item label="应用名称" name="appName">
            <a-input v-model:value="form.appName" placeholder="请输入应用名称" size="large" />
          </a-form-item>

          <!-- 管理员专用字段 -->
          <template v-if="isAdmin">
            <!-- 应用封面 -->
            <a-form-item label="应用封面">
              <div class="cover-upload-section">
                <div class="current-cover">
                  <div v-if="form.cover" class="cover-preview">
                    <img :src="form.cover" alt="封面预览" class="cover-image" />
                  </div>
                  <div v-else class="cover-placeholder">
                    <span>🖼️</span>
                    <p>暂无封面</p>
                  </div>
                </div>
                <div class="cover-actions">
                  <a-upload :show-upload-list="false" accept="image/*" @change="handleCoverUpload">
                    <a-button type="primary" ghost> 上传封面 </a-button>
                  </a-upload>
                  <a-input
                    v-model:value="form.cover"
                    placeholder="或输入封面URL"
                    style="margin-top: 12px"
                  />
                </div>
              </div>
            </a-form-item>

            <!-- 优先级 -->
            <a-form-item label="优先级" name="priority">
              <a-input-number
                v-model:value="form.priority"
                :min="0"
                :max="999"
                placeholder="请输入优先级"
                style="width: 200px"
              />
              <div class="form-help">
                <p>优先级越高，应用在列表中的排序越靠前</p>
                <p>设置为 99 或以上将显示为精选应用</p>
              </div>
            </a-form-item>
          </template>

          <!-- 应用信息展示 -->
          <div class="app-info-section">
            <h3>应用信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <label>应用ID</label>
                <span>{{ app?.id }}</span>
              </div>
              <div class="info-item">
                <label>代码生成类型</label>
                <a-tag
                  :color="
                    app?.codeGenType === 'HTML'
                      ? 'blue'
                      : app?.codeGenType === 'MULTI_FILE'
                        ? 'green'
                        : 'orange'
                  "
                >
                  {{ app?.codeGenType?.toUpperCase() }}
                </a-tag>
              </div>
              <div class="info-item">
                <label>创建时间</label>
                <span>{{ app?.createTime }}</span>
              </div>
              <div class="info-item">
                <label>更新时间</label>
                <span>{{ app?.updateTime }}</span>
              </div>
              <div v-if="isAdmin" class="info-item">
                <label>用户ID</label>
                <span>{{ app?.userId }}</span>
              </div>
            </div>
          </div>

          <!-- 初始提示词展示 -->
          <div class="prompt-section">
            <h3>初始提示词</h3>
            <div class="prompt-content">
              {{ app?.initPrompt }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <a-space size="large">
              <a-button size="large" @click="handleCancel"> 取消 </a-button>
              <a-button type="primary" size="large" html-type="submit" :loading="submitting">
                保存修改
              </a-button>
            </a-space>
          </div>
        </a-form>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.app-edit-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-loading {
  min-height: 400px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 32px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: #666;
  font-size: 16px;
}

.header-info h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.header-info p {
  margin: 4px 0 0 0;
  color: #6b7280;
}

/* 表单容器 */
.form-container {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.edit-form {
  max-width: 600px;
}

/* 封面上传区域 */
.cover-upload-section {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.current-cover {
  flex-shrink: 0;
}

.cover-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  color: #999;
}

.cover-placeholder span {
  font-size: 32px;
  margin-bottom: 8px;
}

.cover-placeholder p {
  margin: 0;
  font-size: 12px;
}

.cover-actions {
  flex: 1;
}

.form-help {
  margin-top: 8px;
}

.form-help p {
  margin: 0;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

/* 应用信息区域 */
.app-info-section,
.prompt-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
}

.app-info-section h3,
.prompt-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.info-item span {
  font-size: 14px;
  color: #1f2937;
}

.prompt-content {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  line-height: 1.6;
  color: #495057;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 操作按钮 */
.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-edit-page {
    padding: 16px;
  }

  .form-container {
    padding: 24px 16px;
  }

  .cover-upload-section {
    flex-direction: column;
    gap: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .header-left {
    gap: 12px;
  }

  .header-info h1 {
    font-size: 20px;
  }
}

@media (max-width: 576px) {
  .form-actions {
    text-align: stretch;
  }

  .form-actions :deep(.ant-space) {
    width: 100%;
    justify-content: stretch;
  }

  .form-actions :deep(.ant-space-item) {
    flex: 1;
  }

  .form-actions :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
