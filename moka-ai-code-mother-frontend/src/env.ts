/**
 * 环境变量配置
 * 用于管理应用的部署域名和预览域名
 */

// 环境变量接口定义
interface EnvConfig {
  // API 基础地址
  API_BASE_URL: string
  // 应用部署域名
  APP_DOMAIN: string
  // 应用预览域名
  PREVIEW_DOMAIN: string
  // 当前环境
  NODE_ENV: string
}

// 获取环境变量的辅助函数
const getEnvVar = (key: string, defaultValue?: string): string => {
  const value = import.meta.env[key] || defaultValue
  if (!value) {
    console.warn(`环境变量 ${key} 未设置`)
  }
  return value || ''
}

// 环境变量配置
export const env: EnvConfig = {
  // API 基础地址
  API_BASE_URL: getEnvVar('VITE_API_BASE_URL', 'http://localhost:8123/api'),

  // 应用部署域名
  APP_DOMAIN: getEnvVar('VITE_APP_DOMAIN', 'http://localhost'),

  // 应用预览域名（当前域名）
  PREVIEW_DOMAIN: getEnvVar(
    'VITE_PREVIEW_DOMAIN',
    typeof window !== 'undefined' ? window.location.origin : 'http://localhost:5173',
  ),

  // 当前环境
  NODE_ENV: getEnvVar('NODE_ENV', 'development'),
}

// 导出常用的环境判断
export const isDevelopment = env.NODE_ENV === 'development'
export const isProduction = env.NODE_ENV === 'production'
export const isTest = env.NODE_ENV === 'test'

// 导出 API 基础地址（向后兼容）
export const API_BASE_URL = env.API_BASE_URL

// 导出域名配置
export const APP_DOMAIN = env.APP_DOMAIN
export const PREVIEW_DOMAIN = env.PREVIEW_DOMAIN

export default env
