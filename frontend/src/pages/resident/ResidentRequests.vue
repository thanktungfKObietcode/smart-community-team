<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import StatusTag from '../../components/StatusTag.vue'
import LoadingState from '../../components/LoadingState.vue'
import EmptyState from '../../components/EmptyState.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate } from '../../utils/status'

const requests = ref([])
const loading = ref(true)
const error = ref('')
const query = ref('')
const status = ref(null)
const dialog = ref(false)
const saving = ref(false)
const formError = ref('')
const form = ref({ title: '', category: 'OTHER', description: '' })
const statuses = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED']
const categories = ['ELECTRICAL', 'PLUMBING', 'ELEVATOR', 'CLEANING', 'OTHER']

const filtered = computed(() => requests.value.filter((item) =>
  (!status.value || item.status === status.value) && `${item.id} ${item.title} ${item.category}`.toLowerCase().includes(query.value.toLowerCase())
))
const load = async () => {
  loading.value = true; error.value = ''
  try { requests.value = (await api.get('/service-requests/my')).data }
  catch (exception) { error.value = errorMessage(exception) }
  finally { loading.value = false }
}
const openCreate = () => { form.value = { title: '', category: 'OTHER', description: '' }; formError.value = ''; dialog.value = true }
const create = async () => {
  formError.value = ''
  if (!form.value.title || !form.value.description) { formError.value = 'Vui lòng nhập tiêu đề và mô tả.'; return }
  saving.value = true
  try { await api.post('/service-requests', form.value); dialog.value = false; await load() }
  catch (exception) { formError.value = errorMessage(exception) }
  finally { saving.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Yêu cầu dịch vụ</h1><p>Theo dõi tiến độ xử lý các vấn đề tại căn hộ.</p></div><Button label="Báo sự cố" icon="pi pi-plus" @click="openCreate" /></div>
  <div class="content-card"><div class="toolbar"><InputText v-model="query" placeholder="Tìm theo mã, tiêu đề..." /><Select v-model="status" :options="statuses" placeholder="Tất cả trạng thái" show-clear /></div><LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div><EmptyState v-else-if="!filtered.length" title="Chưa có yêu cầu phù hợp" /><DataTable v-else :value="filtered" paginator :rows="8" responsive-layout="scroll"><Column header="Mã"><template #body="{ data }"><RouterLink class="code" :to="`/resident/requests/${data.id}`">{{ data.code }}</RouterLink></template></Column><Column field="title" header="Yêu cầu" /><Column field="category" header="Danh mục" /><Column header="Ưu tiên"><template #body="{ data }"><StatusTag :value="data.priority" /></template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column><Column header="Hạn xử lý"><template #body="{ data }"><span :class="{ overdue: data.overdue }">{{ formatDate(data.dueAt) }}</span></template></Column><Column header="Tạo lúc"><template #body="{ data }">{{ formatDate(data.createdAt) }}</template></Column></DataTable></div>
  <Dialog v-model:visible="dialog" modal header="Báo sự cố" :style="{ width: 'min(38rem, 94vw)' }"><Message v-if="formError" severity="error">{{ formError }}</Message><div class="form-grid"><div class="field field-full"><label>Tiêu đề</label><InputText v-model.trim="form.title" maxlength="150" placeholder="Ví dụ: Rò rỉ nước tại bếp" /></div><div class="field"><label>Danh mục</label><Select v-model="form.category" :options="categories" /></div><div class="field field-full"><label>Mô tả chi tiết</label><Textarea v-model.trim="form.description" rows="5" maxlength="2000" placeholder="Mô tả vị trí, mức độ và thời điểm xảy ra..." /></div></div><template #footer><Button label="Hủy" text @click="dialog = false" /><Button label="Gửi yêu cầu" icon="pi pi-send" :loading="saving" @click="create" /></template></Dialog>
</template>
