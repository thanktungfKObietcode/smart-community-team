<script setup>
import Tag from 'primevue/tag'
import { labelFor, severityFor } from '../utils/status'

defineProps({ value: { type: String, default: '' } })
</script>

<template>
  <Tag :value="labelFor(value)" :severity="severityFor(value)" rounded />
</template>
