import axios from 'axios'

import { clearSession, getAccessToken } from '../utils/session'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.skipSessionExpiry) {
      clearSession()
      window.dispatchEvent(new Event('auth:expired'))
    }
    return Promise.reject(error)
  },
)

export function apiErrorMessage(error, fallback = '目前無法完成操作，請稍後再試') {
  if (error.code === 'ECONNABORTED') {
    return '連線逾時，請確認服務是否正常運作'
  }
  if (!error.response) {
    return '無法連線至服務，請確認網路與後端狀態'
  }
  return error.response.data?.message || fallback
}

export function apiFieldErrors(error) {
  return error.response?.data?.errors ?? {}
}
