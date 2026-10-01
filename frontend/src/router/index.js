import { createRouter, createWebHistory } from 'vue-router'
import { defaultRoute, isAuthenticated, hasRole } from '../services/auth'

const pages = {
  login: () => import('../pages/auth/LoginPage.vue'),
  residentDashboard: () => import('../pages/resident/ResidentDashboard.vue'),
  residentRequests: () => import('../pages/resident/ResidentRequests.vue'),
  residentRequestDetail: () => import('../pages/resident/RequestDetail.vue'),
  residentFacilities: () => import('../pages/resident/ResidentFacilities.vue'),
  residentFacilityDetail: () => import('../pages/resident/FacilityDetail.vue'),
  residentBookings: () => import('../pages/resident/ResidentBookings.vue'),
  residentVisitors: () => import('../pages/resident/ResidentVisitors.vue'),
  residentNotifications: () => import('../pages/resident/ResidentNotifications.vue'),
  residentApartment: () => import('../pages/resident/ResidentApartment.vue'),
  managerDashboard: () => import('../pages/manager/ManagerDashboard.vue'),
  managerRequests: () => import('../pages/manager/ManagerRequests.vue'),
  managerResidents: () => import('../pages/manager/ManagerResidents.vue'),
  managerBuildings: () => import('../pages/manager/ManagerBuildings.vue'),
  managerFacilities: () => import('../pages/manager/ManagerFacilities.vue'),
  managerBookings: () => import('../pages/manager/ManagerBookings.vue'),
  managerVisitors: () => import('../pages/manager/ManagerVisitors.vue'),
  managerAudit: () => import('../pages/manager/ManagerAudit.vue'),
  managerAccounts: () => import('../pages/manager/ManagerAccounts.vue'),
  technicianDashboard: () => import('../pages/technician/TechnicianDashboard.vue'),
  technicianTasks: () => import('../pages/technician/TechnicianTasks.vue'),
  securityVisitors: () => import('../pages/security/SecurityVisitors.vue')
}
const layouts = {
  resident: () => import('../layouts/ResidentLayout.vue'),
  manager: () => import('../layouts/ManagerLayout.vue'),
  technician: () => import('../layouts/TechnicianLayout.vue'),
  security: () => import('../layouts/SecurityLayout.vue')
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => defaultRoute() },
    { path: '/login', component: pages.login, meta: { public: true } },
    { path: '/resident', component: layouts.resident, meta: { roles: ['RESIDENT'] }, children: [
      { path: '', redirect: '/resident/dashboard' },
      { path: 'dashboard', component: pages.residentDashboard },
      { path: 'requests', component: pages.residentRequests },
      { path: 'requests/:id', component: pages.residentRequestDetail, props: true },
      { path: 'facilities', component: pages.residentFacilities },
      { path: 'facilities/:id', component: pages.residentFacilityDetail, props: true },
      { path: 'bookings', component: pages.residentBookings },
      { path: 'visitors', component: pages.residentVisitors },
      { path: 'notifications', component: pages.residentNotifications },
      { path: 'apartment', component: pages.residentApartment }
    ] },
    { path: '/manager', component: layouts.manager, meta: { roles: ['ADMIN', 'MANAGER'] }, children: [
      { path: '', redirect: '/manager/dashboard' },
      { path: 'dashboard', component: pages.managerDashboard },
      { path: 'requests', component: pages.managerRequests },
      { path: 'residents', component: pages.managerResidents },
      { path: 'buildings', component: pages.managerBuildings },
      { path: 'facilities', component: pages.managerFacilities },
      { path: 'bookings', component: pages.managerBookings },
      { path: 'visitors', component: pages.managerVisitors },
      { path: 'audit', component: pages.managerAudit },
      { path: 'accounts', component: pages.managerAccounts, meta: { roles: ['ADMIN'] } }
    ] },
    { path: '/technician', component: layouts.technician, meta: { roles: ['TECHNICIAN'] }, children: [
      { path: '', redirect: '/technician/dashboard' },
      { path: 'dashboard', component: pages.technicianDashboard },
      { path: 'tasks', component: pages.technicianTasks }
    ] },
    { path: '/security', component: layouts.security, meta: { roles: ['SECURITY'] }, children: [
      { path: '', redirect: '/security/visitors' },
      { path: 'visitors', component: pages.securityVisitors }
    ] },
    { path: '/:pathMatch(.*)*', redirect: () => defaultRoute() }
  ]
})

router.beforeEach((to) => {
  if (to.meta.public) return isAuthenticated.value ? defaultRoute() : true
  if (!isAuthenticated.value) return '/login'
  const allowed = to.matched.every((record) => {
    const roles = record.meta.roles || []
    return !roles.length || hasRole(...roles)
  })
  return allowed ? true : defaultRoute()
})

export default router
