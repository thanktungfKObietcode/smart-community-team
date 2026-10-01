<script setup>
import { useRoute } from 'vue-router'
const props = defineProps({ items: { type: Array, default: () => [] } })
const route = useRoute()
const active = (item) => route.path.startsWith(item.to.split('?')[0])
</script>
<template>
  <nav v-if="props.items.length" class="mobile-bottom-nav" aria-label="Điều hướng nhanh">
    <RouterLink v-for="item in props.items" :key="item.to" :to="item.to" :class="{ active: active(item) }"><i :class="item.icon" /><span>{{ item.label }}</span></RouterLink>
  </nav>
</template>
