<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [{ label: 'Tra cứu khách', icon: 'pi pi-shield', to: '/security/visitors' }]
</script>
<template><PortalLayout portal="Cổng bảo vệ" :items="items" /></template>
