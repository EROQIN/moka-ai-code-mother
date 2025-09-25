<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { listAppVoByPageAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'

const router = useRouter()

// 响应式数据
const apps = ref<API.AppVO[]>([])
const loading = ref(false)
const searchForm = ref({
  appName: '',
  codeGenType: '' as 'HTML' | 'MULTI_FILE' | '',
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
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    ellipsis: true,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    key: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initPrompt',
    key: 'initPrompt',
    ellipsis: true,
    width: 200,
  },
  {
    title: '代码生成类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 80,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const,
  },
]

// 加载应用列表
const loadApps = async (page = 1, pageSize = 10) => {
  loading.value = true
  try {
    const res = await listAppVoByPageAdmin({
      pageNum: page,
      pageSize,
      appName: searchForm.value.appName || undefined,
      codeGenType: (searchForm.value.codeGenType as 'HTML' | 'MULTI_FILE') || undefined,
      userId: searchForm.value.userId,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      apps.value = res.data.data.records || []
      pagination.value = {
        ...pagination.value,
        current: res.data.data.pageNumber || 1,
        pageSize: res.data.data.pageSize || 10,
        total: res.data.data.totalRow || 0,
      }
    }
  } catch (error) {
    message.error('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.current = 1
  loadApps(1, pagination.value.pageSize)
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    appName: '',
    codeGenType: '',
    userId: undefined,
  }
  pagination.value.current = 1
  loadApps(1, pagination.value.pageSize)
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.value = pag
  loadApps(pag.current, pag.pageSize)
}

// 编辑应用
const handleEdit = (record: API.AppVO) => {
  router.push(`/app/edit/${record.id}`)
}

// 删除应用
const handleDelete = (record: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用"${record.appName}"吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadApps(pagination.value.current, pagination.value.pageSize)
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

// 设为精选
const handleSetFeatured = async (record: API.AppVO) => {
  try {
    const res = await updateAppByAdmin({
      id: record.id,
      priority: 99,
    })
    if (res.data.code === 0) {
      message.success('设为精选成功')
      loadApps(pagination.value.current, pagination.value.pageSize)
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    message.error('操作失败')
  }
}

// 查看应用详情
const handleView = (record: API.AppVO) => {
  router.push(`/app/chat/${record.id}`)
}

// 页面加载时获取数据
onMounted(() => {
  loadApps()
})
</script>

<template>
  <div class="app-manage-page">
    <div class="page-header">
      <h1>应用管理</h1>
      <p>管理系统中的所有应用</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="searchForm.appName"
            placeholder="请输入应用名称"
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="代码类型">
          <a-select
            v-model:value="searchForm.codeGenType"
            placeholder="请选择代码类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="HTML">HTML</a-select-option>
            <a-select-option value="MULTI_FILE">MULTI_FILE</a-select-option>
          </a-select>
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

    <!-- 应用列表表格 -->
    <div class="table-container">
      <a-table
        :columns="columns"
        :data-source="apps"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1200 }"
        row-key="id"
        @change="handleTableChange"
      >
        <!-- 封面列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'cover'">
            <div class="cover-cell">
              <img
                v-if="record.cover"
                :src="record.cover"
                :alt="record.appName"
                class="cover-image"
              />
              <div v-else class="cover-placeholder">🤖</div>
            </div>
          </template>

          <!-- 初始提示词列 -->
          <template v-else-if="column.key === 'initPrompt'">
            <a-tooltip :title="record.initPrompt">
              <span class="text-ellipsis">
                {{ record.initPrompt?.substring(0, 50)
                }}{{ record.initPrompt?.length > 50 ? '...' : '' }}
              </span>
            </a-tooltip>
          </template>

          <!-- 代码生成类型列 -->
          <template v-else-if="column.key === 'codeGenType'">
            <a-tag
              :color="
                record.codeGenType === 'HTML'
                  ? 'blue'
                  : record.codeGenType === 'MULTI_FILE'
                    ? 'green'
                    : 'orange'
              "
            >
              {{ record.codeGenType?.toUpperCase() }}
            </a-tag>
          </template>

          <!-- 优先级列 -->
          <template v-else-if="column.key === 'priority'">
            <a-tag v-if="record.priority >= 99" color="red"> 精选 </a-tag>
            <span v-else>{{ record.priority || 0 }}</span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)"> 查看 </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)"> 编辑 </a-button>
              <a-button
                type="link"
                size="small"
                :disabled="record.priority >= 99"
                @click="handleSetFeatured(record)"
              >
                精选
              </a-button>
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
.app-manage-page {
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

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-placeholder {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 18px;
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
  .app-manage-page {
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
