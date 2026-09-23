<script setup>
import Button from 'primevue/button'
import Menu from 'primevue/menu'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import NotificationBell from './NotificationBell.vue'
import { labelFor } from '../utils/status'

defineEmits(['toggle-menu'])
const menu = ref()
const router = useRouter()
const { currentUser, logout } = useAuth()
const items = [{ label: 'Đăng xuất', icon: 'pi pi-sign-out', command: () => { logout(); router.push('/login') } }]
</script>

<template>
  <header class="app-header">
    <Button class="mobile-menu" icon="pi pi-bars" text rounded @click="$emit('toggle-menu')" />
    <div class="header-title"><span>Smart Community</span><small>Vận hành cộng đồng thông minh</small></div>
    <div class="header-actions">
      <NotificationBell />
      <button class="profile-button" @click="menu.toggle($event)"><span class="avatar">{{ currentUser?.fullName?.slice(0, 1) || 'U' }}</span><span class="profile-name"><strong>{{ currentUser?.fullName }}</strong><small>{{ labelFor(currentUser?.roles?.[0]) }}</small></span><i class="pi pi-angle-down" /></button>
      <Menu ref="menu" :model="items" popup />
    </div>
  </header>
</template>
