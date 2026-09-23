<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Badge from 'primevue/badge'
import Dialog from 'primevue/dialog'
import api from '../services/api'
import { formatDate } from '../utils/status'
import LoadingState from './LoadingState.vue'
import EmptyState from './EmptyState.vue'

const open = ref(false)
const notifications = ref([])
const loading = ref(false)
const unread = computed(() => notifications.value.filter((item) => !item.read).length)

const load = async () => {
  loading.value = true
  try { notifications.value = (await api.get('/notifications/my')).data } finally { loading.value = false }
}
const markRead = async (item) => {
  if (item.read) return
  await api.patch(`/notifications/${item.id}/read`)
  item.read = true
}
const markAll = async () => {
  await api.patch('/notifications/read-all')
  notifications.value.forEach((item) => { item.read = true })
}
const show = async () => { open.value = true; await load() }
onMounted(load)
</script>

<template>
  <span class="notification-bell">
    <Button icon="pi pi-bell" text rounded aria-label="Thông báo" @click="show" />
    <Badge v-if="unread" :value="unread > 9 ? '9+' : unread" severity="danger" />
  </span>
  <Dialog v-model:visible="open" modal header="Thông báo" :style="{ width: 'min(32rem, 94vw)' }">
    <template #header><div class="dialog-heading"><span>Thông báo</span><Button label="Đọc tất cả" text size="small" :disabled="!unread" @click="markAll" /></div></template>
    <LoadingState v-if="loading" />
    <EmptyState v-else-if="!notifications.length" title="Chưa có thông báo" />
    <div v-else class="notification-list">
      <button v-for="item in notifications.slice(0, 8)" :key="item.id" class="notification-item" :class="{ unread: !item.read }" @click="markRead(item)">
        <span class="notification-dot" /><span><strong>{{ item.title }}</strong><small>{{ item.message }}</small><time>{{ formatDate(item.createdAt) }}</time></span>
      </button>
    </div>
  </Dialog>
</template>
