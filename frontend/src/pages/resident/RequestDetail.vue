<script setup>
import { computed, onMounted, ref } from 'vue'
import { useConfirm } from 'primevue/useconfirm'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import Divider from 'primevue/divider'
import LoadingState from '../../components/LoadingState.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'

const route = useRoute(); const router = useRouter(); const confirm = useConfirm(); const request = ref(null); const error = ref(''); const saving = ref(false)
const steps = [
  { key: 'OPEN', label: 'Mới tiếp nhận', field: 'createdAt', text: 'Yêu cầu của bạn đã được tiếp nhận.' },
  { key: 'ASSIGNED', label: 'Đã phân công', field: 'assignedAt', text: 'Ban quản lý đã phân công kỹ thuật viên.' },
  { key: 'IN_PROGRESS', label: 'Đang xử lý', field: 'startedAt', text: 'Kỹ thuật viên đang xử lý sự cố.' },
  { key: 'RESOLVED', label: 'Chờ xác nhận', field: 'resolvedAt', text: 'Công việc đã được cập nhật là hoàn tất.' },
  { key: 'CLOSED', label: 'Hoàn thành', field: 'closedAt', text: 'Bạn đã xác nhận kết quả xử lý.' }
]
const position = computed(() => steps.findIndex((step) => step.key === request.value?.status))
const load = async () => { try { request.value = (await api.get(`/service-requests/${route.params.id}`)).data } catch (e) { error.value = errorMessage(e) } }
const close = () => confirm.require({ header: 'Xác nhận hoàn thành', message: 'Bạn hài lòng với kết quả xử lý sự cố này chứ?', acceptLabel: 'Xác nhận hoàn thành', rejectLabel: 'Xem lại', accept: async () => { saving.value = true; try { await api.patch(`/service-requests/${request.value.id}/confirm`); await load() } catch (e) { error.value = errorMessage(e) } finally { saving.value = false } } })
onMounted(load)
</script>
<template>
  <Button label="Quay lại báo sự cố" icon="pi pi-arrow-left" text @click="router.push('/resident/requests')" />
  <LoadingState v-if="!request && !error" /><div v-else-if="error" class="error-panel">{{ error }}</div>
  <template v-else><div class="page-heading"><div><span class="code">{{ request.code }}</span><h1>{{ request.title }}</h1><p>{{ labelFor(request.category) }} · Gửi lúc {{ formatDate(request.createdAt) }}</p></div><StatusTag :value="request.status" /></div><section class="content-card"><div class="request-timeline"><div v-for="(step, index) in steps" :key="step.key" class="timeline-entry" :class="{ complete: index <= position }"><span class="timeline-dot"><i v-if="index <= position" class="pi pi-check" /></span><div><strong>{{ step.label }}</strong><p>{{ step.text }}</p><small>{{ request[step.field] ? formatDate(request[step.field]) : 'Đang chờ cập nhật' }}</small></div></div></div><Divider /><div class="detail-grid"><div><p class="eyebrow">Nội dung báo sự cố</p><h2>Thông tin bạn đã gửi</h2><p>{{ request.description }}</p><div class="card-meta"><span><i class="pi pi-flag" /> Ưu tiên: {{ labelFor(request.priority) }}</span><span :class="{ overdue: request.overdue }"><i class="pi pi-clock" /> Hạn xử lý: {{ formatDate(request.dueAt) }}</span></div></div><div class="resolution-panel"><p class="eyebrow">Kết quả xử lý</p><h2>{{ request.resolutionNote ? 'Cập nhật từ kỹ thuật viên' : 'Đang chờ xử lý' }}</h2><p>{{ request.resolutionNote || 'Kết quả sẽ được hiển thị khi kỹ thuật viên hoàn tất công việc.' }}</p><Button v-if="request.status === 'RESOLVED'" label="Xác nhận hoàn thành" icon="pi pi-check" :loading="saving" @click="close" /></div></div></section></template>
</template>
