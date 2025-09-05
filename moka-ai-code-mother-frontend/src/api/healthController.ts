import request from '@/request'

/** 此处后端没有提供注释 GET /health/ */
export async function healthCheck(options?: { [key: string]: unknown }) {
  return request<API.BaseResponseString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}
