<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'

const toast = useToast()
const showApiError = (event) => {
  if (event.detail.status === 403) {
    toast.add({ severity: 'warn', summary: 'Không có quyền truy cập', detail: event.detail.message, life: 4000 })
  }
}
onMounted(() => window.addEventListener('app:api-error', showApiError))
onUnmounted(() => window.removeEventListener('app:api-error', showApiError))
</script>

<template>
  <Toast position="top-right" />
  <ConfirmDialog />
  <RouterView />
</template>
