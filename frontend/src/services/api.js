import axios from 'axios'
import { clearSession, getToken } from './auth'

let onUnauthorized = () => {}

const knownMessages = [
  [/email.*(already|exists|used)/i, 'Email này đã được sử dụng.'],
  [/(already booked|overlap)/i, 'Khung giờ này đã có người đặt. Vui lòng chọn thời gian khác.'],
  [/(not available|unavailable)/i, 'Tiện ích hiện chưa thể đặt lịch.'],
  [/(not found)/i, 'Không tìm thấy thông tin bạn yêu cầu.'],
  [/(forbidden|cannot modify|not assigned)/i, 'Bạn không có quyền thực hiện thao tác này.'],
  [/(invalid|required|must be|before)/i, 'Thông tin nhập chưa hợp lệ. Vui lòng kiểm tra lại.']
]

export const setUnauthorizedHandler = (handler) => { onUnauthorized = handler }

export const friendlyError = (error, fallback = 'Đã xảy ra lỗi. Vui lòng thử lại sau.') => {
  const status = error.response?.status
  if (!error.response) return 'Không thể kết nối tới hệ thống. Vui lòng thử lại.'
  if (status === 401) return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này.'
  const raw = String(error.response?.data?.message || '')
  return knownMessages.find(([pattern]) => pattern.test(raw))?.[1] || fallback
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
    if (status === 401 && !error.config?.url?.includes('/auth/login')) {
      clearSession()
      onUnauthorized()
    }
    if ([400, 403, 409].includes(status)) {
      window.dispatchEvent(new CustomEvent('app:api-error', { detail: { status, message: friendlyError(error) } }))
    }
    return Promise.reject(error)
  }
)

export const errorMessage = (error, fallback = 'Thao tác chưa thể hoàn tất.') => friendlyError(error, fallback)
export default api
