import { computed, reactive } from 'vue'
import api from './api'

const TOKEN_KEY = 'smart-community.token'
const USER_KEY = 'smart-community.user'

const readUser = () => {
  try { return JSON.parse(sessionStorage.getItem(USER_KEY)) || null } catch { return null }
}

export const authState = reactive({ token: sessionStorage.getItem(TOKEN_KEY), user: readUser() })

export const getToken = () => authState.token
export const currentUser = computed(() => authState.user)
export const isAuthenticated = computed(() => Boolean(authState.token && authState.user))

export const hasRole = (...roles) => roles.some((role) => authState.user?.roles?.includes(role))

export const defaultRoute = (roles = authState.user?.roles || []) => {
  if (roles.includes('ADMIN') || roles.includes('MANAGER')) return '/manager/dashboard'
  if (roles.includes('RESIDENT')) return '/resident/dashboard'
  if (roles.includes('TECHNICIAN')) return '/technician/dashboard'
  if (roles.includes('SECURITY')) return '/security/visitors'
  return '/login'
}

export const login = async (email, password) => {
  const { data } = await api.post('/auth/login', { email, password })
  authState.token = data.accessToken
  authState.user = { userId: data.userId, fullName: data.fullName, email: data.email, roles: data.roles || [] }
  sessionStorage.setItem(TOKEN_KEY, data.accessToken)
  sessionStorage.setItem(USER_KEY, JSON.stringify(authState.user))
  return authState.user
}

export const refreshCurrentUser = async () => {
  const { data } = await api.get('/auth/me')
  authState.user = data
  sessionStorage.setItem(USER_KEY, JSON.stringify(data))
  return data
}

export const clearSession = () => {
  authState.token = null
  authState.user = null
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export const logout = () => clearSession()
