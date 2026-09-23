<script setup>
import Drawer from 'primevue/drawer'
import { useRoute } from 'vue-router'

const props = defineProps({ items: { type: Array, default: () => [] }, mobileOpen: Boolean, portal: String })
const emit = defineEmits(['update:mobileOpen'])
const route = useRoute()
const isActive = (item) => route.path.startsWith(item.to)
</script>

<template>
  <aside class="sidebar desktop-sidebar">
    <div class="brand"><span class="brand-mark"><i class="pi pi-building" /></span><div><strong>Smart Community</strong><small>{{ portal }}</small></div></div>
    <nav><RouterLink v-for="item in items" :key="item.to" :to="item.to" :class="{ active: isActive(item) }"><i :class="item.icon" /><span>{{ item.label }}</span></RouterLink></nav>
    <div class="sidebar-footer"><i class="pi pi-shield" /> Vận hành an toàn</div>
  </aside>
  <Drawer :visible="props.mobileOpen" position="left" class="mobile-drawer" @update:visible="emit('update:mobileOpen', $event)">
    <div class="brand"><span class="brand-mark"><i class="pi pi-building" /></span><div><strong>Smart Community</strong><small>{{ portal }}</small></div></div>
    <nav><RouterLink v-for="item in items" :key="item.to" :to="item.to" :class="{ active: isActive(item) }" @click="emit('update:mobileOpen', false)"><i :class="item.icon" /><span>{{ item.label }}</span></RouterLink></nav>
  </Drawer>
</template>
