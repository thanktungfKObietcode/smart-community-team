<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import Textarea from 'primevue/textarea'
import api, { errorMessage } from '../../services/api'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import StatusTag from '../../components/StatusTag.vue'
import { formatDate, labelFor } from '../../utils/status'

const tasks = ref([]); const loading = ref(true); const error = ref(''); const submitting = ref(false); const resolveDialog = ref(false); const current = ref(null); const resolutionNote = ref('')
const load = async () => { loading.value = true; try { tasks.value = (await api.get('/technician/service-requests/my')).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false } }
const start = async (task) => { try { await api.patch(`/technician/service-requests/${task.id}/start`); await load() } catch (exception) { error.value = errorMessage(exception) } }
const openResolve = (task) => { current.value = task; resolutionNote.value = ''; resolveDialog.value = true }
const resolve = async () => { if (!resolutionNote.value.trim()) { error.value = 'Vui lòng nhập ghi chú xử lý.'; return }; submitting.value = true; try { await api.patch(`/technician/service-requests/${current.value.id}/resolve`, { resolutionNote: resolutionNote.value }); resolveDialog.value = false; await load() } catch (exception) { error.value = errorMessage(exception) } finally { submitting.value = false } }
onMounted(load)
</script>

<template><div class="page-heading"><div><h1>Danh sách công việc</h1><p>Cập nhật tiến độ cho các yêu cầu được phân công.</p></div></div><Message v-if="error" severity="error" class="page-message">{{ error }}</Message><LoadingState v-if="loading" /><EmptyState v-else-if="!tasks.length" title="Chưa có công việc" description="Các yêu cầu được phân công sẽ xuất hiện tại đây." /><section v-else class="task-grid"><article v-for="task in tasks" :key="task.id" class="task-card"><div class="card-title"><div><span class="muted">{{ task.code }}</span><h2>{{ task.title }}</h2></div><StatusTag :value="task.status" /></div><p>{{ task.description }}</p><div class="card-meta"><span>{{ labelFor(task.category) }}</span><span>Ưu tiên: {{ labelFor(task.priority) }}</span><span>Hạn: {{ formatDate(task.dueAt) }}</span><span v-if="task.overdue" class="danger-text">Quá hạn SLA</span></div><div class="inline-actions"><Button v-if="task.status === 'ASSIGNED'" label="Bắt đầu xử lý" icon="pi pi-play" @click="start(task)" /><Button v-if="task.status === 'IN_PROGRESS'" label="Hoàn thành" icon="pi pi-check" @click="openResolve(task)" /></div></article></section><Dialog v-model:visible="resolveDialog" modal header="Hoàn thành yêu cầu" :style="{ width: 'min(34rem, 94vw)' }"><div class="field"><label>Ghi chú xử lý</label><Textarea v-model="resolutionNote" rows="5" placeholder="Mô tả công việc đã thực hiện" /></div><template #footer><Button label="Hủy" text @click="resolveDialog = false" /><Button label="Xác nhận hoàn thành" :loading="submitting" @click="resolve" /></template></Dialog></template>
