<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [
  { label: 'Công việc của tôi', icon: 'pi pi-wrench', to: '/technician/tasks' },
  { label: 'Tổng quan công việc', icon: 'pi pi-chart-bar', to: '/technician/dashboard' }
]
</script>
<template><PortalLayout portal="Cổng kỹ thuật" :items="items" /></template>
