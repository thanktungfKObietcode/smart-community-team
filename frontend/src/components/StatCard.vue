<script setup>
defineProps({ label: String, value: [String, Number], icon: String, tone: { type: String, default: 'teal' }, hint: String })
</script>

<template>
  <article class="stat-card" :class="`tone-${tone}`">
    <div>
      <p>{{ label }}</p>
      <strong>{{ value ?? 0 }}</strong>
      <small v-if="hint">{{ hint }}</small>
    </div>
    <span class="stat-icon"><i :class="icon" /></span>
  </article>
</template>
