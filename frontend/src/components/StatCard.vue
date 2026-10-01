<script setup>
defineProps({ label: String, value: [String, Number], icon: String, tone: { type: String, default: 'teal' }, hint: String, to: String })
</script>

<template>
  <component :is="to ? 'RouterLink' : 'article'" :to="to" class="stat-card" :class="[`tone-${tone}`, { actionable: to }]">
    <div>
      <p>{{ label }}</p>
      <strong>{{ value ?? 0 }}</strong>
      <small v-if="hint">{{ hint }}</small>
    </div>
    <span class="stat-icon"><i :class="icon" /></span>
  </component>
</template>
