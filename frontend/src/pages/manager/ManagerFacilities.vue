<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import ToggleSwitch from 'primevue/toggleswitch'
import api, { errorMessage } from '../../services/api'
import LoadingState from '../../components/LoadingState.vue'
import StatusTag from '../../components/StatusTag.vue'
import { labelFor } from '../../utils/status'

const facilities = ref([])
const loading = ref(true)
const visible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const error = ref('')
const types = ['BADMINTON_COURT', 'COMMUNITY_ROOM', 'READING_ROOM', 'GYM', 'MEETING_ROOM', 'OTHER']
const statuses = ['AVAILABLE', 'MAINTENANCE', 'OUT_OF_SERVICE']
const blank = () => ({ code: '', name: '', description: '', buildingId: null, type: 'OTHER', status: 'AVAILABLE', bookable: true, openingTime: '06:00', closingTime: '22:00', active: true })
const form = ref(blank())
const load = async () => { loading.value = true; try { facilities.value = (await api.get('/facilities')).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false } }
const create = () => { editing.value = null; form.value = blank(); visible.value = true }
const edit = (item) => { editing.value = item.id; form.value = { ...item }; visible.value = true }
const save = async () => {
  error.value = ''
  if (!form.value.name || (!editing.value && !form.value.code)) { error.value = 'Mã và tên tiện ích là bắt buộc.'; return }
  submitting.value = true
  const payload = editing.value
    ? {
        name: form.value.name, description: form.value.description, buildingId: form.value.buildingId,
        type: form.value.type, status: form.value.status, bookable: form.value.bookable,
        openingTime: form.value.openingTime, closingTime: form.value.closingTime, active: form.value.active
      }
    : {
        code: form.value.code, name: form.value.name, description: form.value.description,
        buildingId: form.value.buildingId, type: form.value.type, bookable: form.value.bookable,
        openingTime: form.value.openingTime, closingTime: form.value.closingTime
      }
  try { if (editing.value) await api.patch(`/management/facilities/${editing.value}`, payload); else await api.post('/management/facilities', payload); visible.value = false; await load() } catch (exception) { error.value = errorMessage(exception) } finally { submitting.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Tiện ích</h1><p>Danh mục và tình trạng không gian dùng chung.</p></div><Button label="Tạo tiện ích" icon="pi pi-plus" @click="create" /></div>
  <Message v-if="error" severity="error" class="page-message">{{ error }}</Message><LoadingState v-if="loading" />
  <div v-else class="content-card table-card"><DataTable :value="facilities" responsive-layout="scroll"><Column field="code" header="Mã" /><Column field="name" header="Tiện ích" /><Column header="Loại"><template #body="{ data }">{{ labelFor(data.type) }}</template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column><Column header="Giờ mở"><template #body="{ data }">{{ data.openingTime }} – {{ data.closingTime }}</template></Column><Column header="Đặt chỗ"><template #body="{ data }">{{ data.bookable ? 'Có' : 'Không' }}</template></Column><Column header=""><template #body="{ data }"><Button icon="pi pi-pencil" text rounded @click="edit(data)" /></template></Column></DataTable></div>
  <Dialog v-model:visible="visible" modal :header="editing ? 'Cập nhật tiện ích' : 'Tạo tiện ích'" :style="{ width: 'min(42rem, 94vw)' }"><div class="form-grid"><div class="field"><label>Mã</label><InputText v-model.trim="form.code" :disabled="!!editing" /></div><div class="field"><label>Tên</label><InputText v-model.trim="form.name" /></div><div class="field field-full"><label>Mô tả</label><Textarea v-model="form.description" rows="3" /></div><div class="field"><label>Loại</label><Select v-model="form.type" :options="types" /></div><div class="field"><label>Trạng thái</label><Select v-model="form.status" :options="statuses" /></div><div class="field"><label>Building ID (nếu có)</label><InputNumber v-model="form.buildingId" :use-grouping="false" /></div><div class="field"><label>Giờ mở</label><InputText v-model="form.openingTime" placeholder="06:00" /></div><div class="field"><label>Giờ đóng</label><InputText v-model="form.closingTime" placeholder="22:00" /></div><div class="field"><label>Cho phép đặt</label><ToggleSwitch v-model="form.bookable" /></div><div class="field"><label>Hoạt động</label><ToggleSwitch v-model="form.active" /></div></div><template #footer><Button label="Hủy" text @click="visible = false" /><Button label="Lưu" :loading="submitting" @click="save" /></template></Dialog>
</template>
