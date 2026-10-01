<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Badge from 'primevue/badge'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import EmptyState from './EmptyState.vue'
import LoadingState from './LoadingState.vue'
import api from '../services/api'
import { currentUser } from '../services/auth'
import { formatDate } from '../utils/status'
import { notificationMessage, notificationTitle } from '../utils/notification'

const router = useRouter(); const open = ref(false); const notifications = ref([]); const loading = ref(false)
const unread = computed(() => notifications.value.filter((item) => !item.read).length)
const destination = (item) => {
  const role = currentUser.value?.roles?.[0]
  if (String(item.type).includes('BOOKING')) return '/resident/bookings'
  if (String(item.type).includes('VISITOR')) return role === 'SECURITY' ? '/security/visitors' : '/resident/visitors'
  if (String(item.type).includes('SERVICE_REQUEST')) return role === 'TECHNICIAN' ? '/technician/tasks' : role === 'RESIDENT' ? '/resident/requests' : '/manager/requests'
  return role === 'RESIDENT' ? '/resident/notifications' : null
}
const load = async () => { loading.value = true; try { notifications.value = (await api.get('/notifications/my')).data } finally { loading.value = false } }
const select = async (item) => { if (!item.read) { await api.patch(`/notifications/${item.id}/read`); item.read = true }; const to = destination(item); if (to) { open.value = false; router.push(to) } }
const markAll = async () => { await api.patch('/notifications/read-all'); notifications.value.forEach((item) => { item.read = true }) }
const show = async () => { open.value = true; await load() }
onMounted(load)
</script>
<template>
  <span class="notification-bell"><Button icon="pi pi-bell" text rounded aria-label="Mở thông báo" @click="show" /><Badge v-if="unread" :value="unread > 9 ? '9+' : unread" severity="danger" /></span>
  <Dialog v-model:visible="open" modal :style="{ width: 'min(32rem, 94vw)' }"><template #header><div class="dialog-heading"><span>Thông báo</span><Button label="Đọc tất cả" text size="small" :disabled="!unread" @click="markAll" /></div></template><LoadingState v-if="loading" /><EmptyState v-else-if="!notifications.length" title="Bạn đã xem hết thông báo" text="Các cập nhật mới sẽ xuất hiện tại đây." /><div v-else class="notification-list"><button v-for="item in notifications.slice(0, 8)" :key="item.id" class="notification-item" :class="{ unread: !item.read }" @click="select(item)"><span class="notification-dot" /><span><strong>{{ notificationTitle(item) }}</strong><small>{{ notificationMessage(item) }}</small><time>{{ formatDate(item.createdAt) }}</time></span></button></div></Dialog>
</template>
