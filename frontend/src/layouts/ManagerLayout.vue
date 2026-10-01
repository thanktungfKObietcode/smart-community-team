<script setup>
import { computed } from 'vue'
import PortalLayout from './PortalLayout.vue'
import { useAuth } from '../composables/useAuth'
const { hasRole } = useAuth()
const items = computed(() => [
  { label: 'Tổng quan', icon: 'pi pi-chart-bar', to: '/manager/dashboard' },
  { label: 'Yêu cầu cư dân', icon: 'pi pi-inbox', to: '/manager/requests' },
  { label: 'Phân công kỹ thuật', icon: 'pi pi-wrench', to: '/manager/requests?view=assignment' },
  { label: 'Tiện ích', icon: 'pi pi-th-large', to: '/manager/facilities' },
  { label: 'Lịch đặt', icon: 'pi pi-calendar', to: '/manager/bookings' },
  { label: 'Khách ra vào', icon: 'pi pi-users', to: '/manager/visitors' },
  { label: 'Tòa nhà & căn hộ', icon: 'pi pi-building', to: '/manager/buildings' },
  { label: 'Cư dân', icon: 'pi pi-users', to: '/manager/residents' },
  { label: 'Nhật ký hoạt động', icon: 'pi pi-history', to: '/manager/audit' },
  ...(hasRole('ADMIN') ? [{ label: 'Tài khoản & nhân sự', icon: 'pi pi-id-card', to: '/manager/accounts' }] : [])
])
const portal = computed(() => hasRole('ADMIN') ? 'Cổng quản trị' : 'Cổng ban quản lý')
</script>
<template><PortalLayout :portal="portal" :items="items" /></template>
