<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [
  { label: 'Trang chủ', icon: 'pi pi-home', to: '/resident/dashboard' },
  { label: 'Báo sự cố', icon: 'pi pi-wrench', to: '/resident/requests' },
  { label: 'Tiện ích', icon: 'pi pi-th-large', to: '/resident/facilities' },
  { label: 'Lịch đặt của tôi', icon: 'pi pi-calendar', to: '/resident/bookings' },
  { label: 'Khách của tôi', icon: 'pi pi-users', to: '/resident/visitors' },
  { label: 'Thông báo', icon: 'pi pi-bell', to: '/resident/notifications' },
  { label: 'Thông tin căn hộ', icon: 'pi pi-building', to: '/resident/apartment' }
]
const mobileItems = [items[0], items[1], items[2], items[4]]
</script>
<template><PortalLayout portal="Cổng cư dân" :items="items" :mobile-items="mobileItems" /></template>
