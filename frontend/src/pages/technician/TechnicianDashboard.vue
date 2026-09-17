<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'
import LoadingState from '../../components/LoadingState.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'

const data = ref(null)
const error = ref('')
const load = async () => {
  try { data.value = (await api.get('/technician/dashboard')).data }
  catch (exception) { error.value = errorMessage(exception) }
}
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Công việc của tôi</h1><p>Ưu tiên các yêu cầu đang chờ xử lý và sắp đến hạn.</p></div><Button label="Tất cả công việc" icon="pi pi-list" outlined @click="$router.push('/technician/tasks')" /></div>
  <div v-if="error" class="error-panel">{{ error }}</div>
  <LoadingState v-else-if="!data" />
  <template v-else>
    <section class="kpi-grid"><StatCard label="Được giao" :value="data.assignedTasks" icon="pi pi-inbox" tone="blue" /><StatCard label="Đang xử lý" :value="data.inProgressTasks" icon="pi pi-cog" tone="amber" /><StatCard label="Đã xử lý" :value="data.resolvedTasks" icon="pi pi-check-circle" tone="teal" /><StatCard label="Quá hạn" :value="data.overdueAssignedTasks" icon="pi pi-exclamation-triangle" tone="rose" /></section>
    <section class="content-card"><h2 class="section-title">Công việc gần đây</h2><div v-if="!data.recentTasks?.length" class="muted">Chưa có công việc được giao.</div><div v-else class="task-grid"><article v-for="task in data.recentTasks" :key="task.id" class="task-card"><div class="card-title"><strong>{{ task.code }} · {{ task.title }}</strong><StatusTag :value="task.status" /></div><div class="card-meta"><span>{{ labelFor(task.category) }}</span><span>Hạn: {{ formatDate(task.dueAt) }}</span><span v-if="task.overdue" class="danger-text">Quá hạn</span></div><Button label="Mở công việc" text size="small" @click="$router.push('/technician/tasks')" /></article></div></section>
  </template>
</template>
