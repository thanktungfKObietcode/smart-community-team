<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import StatusTag from '../../components/StatusTag.vue'
import LoadingState from '../../components/LoadingState.vue'
import EmptyState from '../../components/EmptyState.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'

const requests = ref([])
const technicians = ref([])
const loading = ref(true)
const error = ref('')
const query = ref('')
const status = ref(null)
const assignDialog = ref(false)
const priorityDialog = ref(false)
const selected = ref(null)
const technician = ref(null)
const priority = ref('NORMAL')
const saving = ref(false)
const formError = ref('')
const statuses = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED']
const priorities = ['LOW', 'NORMAL', 'HIGH', 'CRITICAL']

const filtered = computed(() => requests.value.filter((item) =>
  (!status.value || item.status === status.value) &&
  `${item.code} ${item.title} ${item.category} ${item.apartmentId}`.toLowerCase().includes(query.value.toLowerCase())
))
const technicianName = (id) => technicians.value.find((item) => item.userId === id)?.fullName || 'Chưa phân công'
const load = async () => {
  loading.value = true; error.value = ''
  try {
    const [requestResponse, technicianResponse] = await Promise.all([
      api.get('/management/service-requests'), api.get('/management/technicians')
    ])
    requests.value = requestResponse.data
    technicians.value = technicianResponse.data
  } catch (exception) { error.value = errorMessage(exception) }
  finally { loading.value = false }
}
const openAssign = (item) => {
  selected.value = item
  technician.value = technicians.value.find((candidate) => candidate.userId === item.assignedTechnicianId) || null
  formError.value = ''; assignDialog.value = true
}
const assign = async () => {
  formError.value = ''
  if (!technician.value) { formError.value = 'Vui lòng chọn kỹ thuật viên.'; return }
  saving.value = true
  try { await api.patch(`/management/service-requests/${selected.value.id}/assign`, { technicianId: technician.value.userId }); assignDialog.value = false; await load() }
  catch (exception) { formError.value = errorMessage(exception) }
  finally { saving.value = false }
}
const openPriority = (item) => { selected.value = item; priority.value = item.priority; formError.value = ''; priorityDialog.value = true }
const updatePriority = async () => {
  saving.value = true; formError.value = ''
  try { await api.patch(`/management/service-requests/${selected.value.id}/priority`, { priority: priority.value }); priorityDialog.value = false; await load() }
  catch (exception) { formError.value = errorMessage(exception) }
  finally { saving.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Điều phối yêu cầu</h1><p>Phân công kỹ thuật viên và theo dõi SLA xử lý.</p></div></div>
  <div class="content-card"><div class="toolbar"><InputText v-model="query" placeholder="Tìm theo mã, căn hộ, nội dung..." /><Select v-model="status" :options="statuses" placeholder="Tất cả trạng thái" show-clear /></div><LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div><EmptyState v-else-if="!filtered.length" title="Chưa có yêu cầu" /><DataTable v-else :value="filtered" paginator :rows="10" responsive-layout="scroll"><Column header="Mã"><template #body="{ data }"><strong class="code">{{ data.code }}</strong></template></Column><Column header="Căn hộ / cư dân"><template #body="{ data }">Căn hộ #{{ data.apartmentId }}<br /><span class="muted">Cư dân #{{ data.residentUserId }}</span></template></Column><Column field="title" header="Nội dung" /><Column header="Danh mục"><template #body="{ data }">{{ labelFor(data.category) }}</template></Column><Column header="Ưu tiên"><template #body="{ data }"><StatusTag :value="data.priority" /></template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column><Column header="Kỹ thuật viên"><template #body="{ data }">{{ technicianName(data.assignedTechnicianId) }}</template></Column><Column header="Hạn SLA"><template #body="{ data }"><span :class="{ overdue: data.overdue }">{{ formatDate(data.dueAt) }}</span></template></Column><Column header=""><template #body="{ data }"><div class="card-actions"><Button v-if="data.status === 'OPEN'" icon="pi pi-user-plus" text rounded aria-label="Phân công" @click="openAssign(data)" /><Button v-if="['OPEN', 'ASSIGNED'].includes(data.status)" icon="pi pi-flag" text rounded aria-label="Ưu tiên" @click="openPriority(data)" /></div></template></Column></DataTable></div>
  <Dialog v-model:visible="assignDialog" modal header="Phân công kỹ thuật viên" :style="{ width: 'min(34rem, 94vw)' }"><Message v-if="formError" severity="error">{{ formError }}</Message><div class="field"><label>Kỹ thuật viên</label><Select v-model="technician" :options="technicians" option-label="fullName" placeholder="Chọn kỹ thuật viên" class="full-width"><template #option="slot"><div><strong>{{ slot.option.fullName }}</strong><small class="select-detail">{{ slot.option.email }}</small></div></template><template #value="slot"><span v-if="slot.value">{{ slot.value.fullName }} · {{ slot.value.email }}</span><span v-else>{{ slot.placeholder }}</span></template></Select></div><div class="dialog-footer"><Button label="Hủy" text @click="assignDialog = false" /><Button label="Phân công" :loading="saving" @click="assign" /></div></Dialog>
  <Dialog v-model:visible="priorityDialog" modal header="Cập nhật ưu tiên" :style="{ width: 'min(30rem, 94vw)' }"><Message v-if="formError" severity="error">{{ formError }}</Message><div class="field"><label>Mức ưu tiên</label><Select v-model="priority" :options="priorities"><template #option="slot"><StatusTag :value="slot.option" /></template><template #value="slot"><StatusTag v-if="slot.value" :value="slot.value" /><span v-else>{{ slot.placeholder }}</span></template></Select></div><div class="dialog-footer"><Button label="Hủy" text @click="priorityDialog = false" /><Button label="Lưu ưu tiên" :loading="saving" @click="updatePriority" /></div></Dialog>
</template>
