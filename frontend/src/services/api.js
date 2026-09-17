import axios from 'axios'
import { clearSession, getToken } from './auth'

let onUnauthorized = () => {}

export const setUnauthorizedHandler = (handler) => {
  onUnauthorized = handler
}

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || 'Không thể kết nối tới hệ thống.'
    if (status === 401 && !error.config?.url?.includes('/auth/login')) {
      clearSession()
      onUnauthorized()
    }
    if ([400, 403, 409].includes(status)) {
      window.dispatchEvent(new CustomEvent('app:api-error', { detail: { status, message } }))
    }
    return Promise.reject(error)
  }
)

export const errorMessage = (error, fallback = 'Thao tác chưa thể hoàn tất.') =>
  error.response?.data?.message || fallback

export default api
