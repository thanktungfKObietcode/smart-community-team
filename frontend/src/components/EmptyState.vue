<script setup>
defineProps({ icon: { type: String, default: 'pi pi-inbox' }, title: { type: String, default: 'Chưa có dữ liệu' }, text: { type: String, default: 'Dữ liệu sẽ xuất hiện tại đây khi có phát sinh.' } })
</script>
<template><div class="state-panel empty"><i :class="icon" /><h3>{{ title }}</h3><p>{{ text }}</p><slot /></div></template>
