<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { listAllChatHistoryByPageForAdmin, remove1 } from '@/api/chatHistoryController'

const router = useRouter()

// 响应式数据
const chatHistories = ref<API.ChatHistory[]>([])
const loading = ref(false)
const searchForm = ref({
  message: '',
  messageType: '' as 'USER' | 'ASSISTANT' | '',
  appId: undefined as number | undefined,
  userId: undefined as number | undefined,
})

// 分页参数
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`,
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    key: 'appId',
    width: 100,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
  },
]

// 加载对话历史列表
const loadChatHistories = async (page = 1, pageSize = 10) => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({
      pageNum: page,
      pageSize,
      message: searchForm.value.message || undefined,
      messageType: searchForm.value.messageType || undefined,
      appId: searchForm.value.appId,
      userId: searchForm.value.userId,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      chatHistories.value = res.data.data.records || []
      pagination.value = {
        ...pagination.value,
        current: res.data.data.pageNumber || 1,
        pageSize: res.data.data.pageSize || 10,
        total: res.data.data.totalRow || 0,
      }
    }
  } catch (error) {
    message.error('加载对话历史列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadChatHistories(1, pagination.value.pageSize)
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    message: '',
    messageType: '',
    appId: undefined,
    userId: undefined,
  }
  pagination.value.current = 1
  loadChatHistories(1, pagination.value.pageSize)
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.value = pag
  loadChatHistories(pag.current, pag.pageSize)
}

// 查看消息详情
const handleView = (record: API.ChatHistory) => {
  Modal.info({
    title: '消息详情',
    width: 600,
    content: `
      <div>
        <p><strong>消息ID:</strong>${record.id}</p>
        <p><strong>消息类型:</strong>${record.messageType}</p>
        <p><strong>应用ID:</strong>${record.appId}</p>
        <p><strong>用户ID:</strong>${record.userId}</p>
        <p><strong>创建时间:</strong>${record.createTime}</p>
        <p><strong>更新时间:</strong>${record.updateTime}</p>
        <p><strong>消息内容:</strong></p>
        <div style="background: #f5f5f5; padding: 12px; border-radius: 6px; margin-top: 8px; max-height: 200px; overflow-y: auto;">
          ${record.message}
        </div>
      </div>
    `,
    okText: '关闭',
  })
}

// 删除对话历史
const handleDelete = (record: API.ChatHistory) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除这条对话历史记录吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await remove1({ id: record.id! })
        if (res.data === true) {
          message.success('删除成功')
          loadChatHistories(pagination.value.current, pagination.value.pageSize)
        } else {
          message.error('删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadChatHistories()
})
</script>

<template>
  <div class="chat-history-manage-page">
    <div class="page-header">
      <h1>对话历史管理</h1>
      <p>管理系统中的所有对话历史记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="消息内容">
          <a-input
            v-model:value="searchForm.message"
            placeholder="请输入消息内容"
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchForm.messageType"
            placeholder="请选择消息类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="USER">用户消息</a-select-option>
            <a-select-option value="ASSISTANT">AI消息</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用ID">
          <a-input-number
            v-model:value="searchForm.appId"
            placeholder="请输入应用ID"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input-number
            v-model:value="searchForm.userId"
            placeholder="请输入用户ID"
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch"> 搜索 </a-button>
            <a-button @click="handleReset"> 重置 </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 对话历史列表表格 -->
    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="chatHistories"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      >
        <!-- 消息类型列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag :color="record.messageType === 'USER' ? 'blue' : 'green'">
              {{ record.messageType === 'USER' ? '用户消息' : 'AI消息' }}
            </a-tag>
          </template>

          <!-- 消息内容列 -->
          <template v-else-if="column.key === 'message'">
            <div class="text-ellipsis" :title="record.message">
              {{ record.message?.substring(0, 50)
              }}{{ record.message && record.message.length > 50 ? '...' : '' }}
            </div>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)"> 查看 </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped>
.chat-history-manage-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
}

.search-form {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.text-ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-history-manage-page {
    padding: 16px;
  }

  .search-form {
    padding: 16px;
  }

  .search-form :deep(.ant-form-item) {
    margin-bottom: 16px;
  }

  .search-form :deep(.ant-form-inline .ant-form-item) {
    display: block;
    width: 100%;
  }
}
</style>
