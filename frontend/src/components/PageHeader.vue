<script setup>
defineProps({ title: String, description: String, eyebrow: String })
</script>
<template>
  <div class="page-heading page-header">
    <div><span v-if="eyebrow" class="eyebrow">{{ eyebrow }}</span><h1>{{ title }}</h1><p v-if="description">{{ description }}</p></div>
    <div class="page-header-actions"><slot /></div>
  </div>
</template>
