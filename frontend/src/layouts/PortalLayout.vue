<script setup>
import { ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import AppSidebar from '../components/AppSidebar.vue'

defineProps({ portal: String, items: Array })
const mobileOpen = ref(false)
</script>

<template>
  <div class="portal-shell"><AppSidebar :portal="portal" :items="items" v-model:mobile-open="mobileOpen" /><div class="portal-main"><AppHeader @toggle-menu="mobileOpen = true" /><main class="page-content"><RouterView /></main></div></div>
</template>
