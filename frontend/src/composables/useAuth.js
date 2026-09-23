import { currentUser, defaultRoute, hasRole, isAuthenticated, login, logout } from '../services/auth'

export const useAuth = () => ({ currentUser, isAuthenticated, hasRole, login, logout, defaultRoute })
