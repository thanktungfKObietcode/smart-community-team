<script setup>
import ProgressSpinner from 'primevue/progressspinner'
defineProps({ message: { type: String, default: 'Đang tải dữ liệu...' } })
</script>
<template><div class="state-panel"><ProgressSpinner style="width:2.25rem;height:2.25rem" /><p>{{ message }}</p></div></template>
