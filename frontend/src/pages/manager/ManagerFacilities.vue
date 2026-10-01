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
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatTime, labelFor } from '../../utils/status'

const facilities = ref([]); const buildings = ref([]); const loading = ref(true); const visible = ref(false); const submitting = ref(false); const editing = ref(null); const error = ref('')
const types = ['BADMINTON_COURT', 'COMMUNITY_ROOM', 'READING_ROOM', 'GYM', 'MEETING_ROOM', 'OTHER']; const statuses = ['AVAILABLE', 'MAINTENANCE', 'OUT_OF_SERVICE']
const blank = () => ({ code: '', name: '', description: '', buildingId: null, location: '', capacity: null, coverImageUrl: '', type: 'OTHER', status: 'AVAILABLE', bookable: true, openingTime: '06:00', closingTime: '22:00', active: true })
const form = ref(blank())
const load = async () => { loading.value = true; error.value = ''; try { const [facilityResponse, buildingResponse] = await Promise.all([api.get('/facilities'), api.get('/buildings')]); facilities.value = facilityResponse.data; buildings.value = buildingResponse.data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
const create = () => { editing.value = null; form.value = blank(); visible.value = true }
const edit = (item) => { editing.value = item.id; form.value = { ...blank(), ...item, location: item.location || '', coverImageUrl: item.coverImageUrl || '' }; visible.value = true }
const payload = () => ({ name: form.value.name, description: form.value.description || null, buildingId: form.value.buildingId, location: form.value.location || null, capacity: form.value.capacity, coverImageUrl: form.value.coverImageUrl || null, type: form.value.type, status: form.value.status, bookable: form.value.bookable, openingTime: form.value.openingTime, closingTime: form.value.closingTime, active: form.value.active })
const save = async () => { if (!form.value.name || (!editing.value && !form.value.code)) { error.value = 'Vui lòng nhập mã và tên tiện ích.'; return }; submitting.value = true; error.value = ''; try { if (editing.value) await api.patch(`/management/facilities/${editing.value}`, payload()); else await api.post('/management/facilities', { code: form.value.code, ...payload() }); visible.value = false; await load() } catch (e) { error.value = errorMessage(e) } finally { submitting.value = false } }
onMounted(load)
</script>
<template>
  <PageHeader title="Tiện ích" description="Quản lý thông tin, khả năng phục vụ và trạng thái các không gian chung."><Button label="Thêm tiện ích" icon="pi pi-plus" @click="create" /></PageHeader>
  <Message v-if="error" severity="error" class="page-message">{{ error }}</Message><LoadingState v-if="loading" />
  <div v-else class="content-card table-card"><DataTable :value="facilities" responsive-layout="scroll"><Column field="code" header="Mã" /><Column field="name" header="Tiện ích" /><Column header="Vị trí"><template #body="{ data }">{{ data.location || 'Khu tiện ích chung' }}</template></Column><Column header="Loại"><template #body="{ data }">{{ labelFor(data.type) }}</template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column><Column header="Giờ hoạt động"><template #body="{ data }">{{ formatTime(data.openingTime) }} – {{ formatTime(data.closingTime) }}</template></Column><Column header="Sức chứa"><template #body="{ data }">{{ data.capacity ? `${data.capacity} người` : '—' }}</template></Column><Column header=""><template #body="{ data }"><Button icon="pi pi-pencil" text rounded aria-label="Cập nhật tiện ích" @click="edit(data)" /></template></Column></DataTable></div>
  <Dialog v-model:visible="visible" modal :header="editing ? 'Cập nhật tiện ích' : 'Thêm tiện ích'" :style="{ width: 'min(44rem, 94vw)' }"><Message v-if="error" severity="error">{{ error }}</Message><div class="form-section"><h3>Thông tin cơ bản</h3><div class="form-grid"><div class="field"><label for="facility-code">Mã tiện ích <span class="required">*</span></label><InputText id="facility-code" v-model.trim="form.code" :disabled="!!editing" placeholder="Ví dụ: GYM-A2" /></div><div class="field"><label for="facility-name">Tên tiện ích <span class="required">*</span></label><InputText id="facility-name" v-model.trim="form.name" /></div><div class="field full"><label for="facility-description">Mô tả</label><Textarea id="facility-description" v-model="form.description" rows="3" placeholder="Không gian, mục đích sử dụng và lưu ý..." /></div><div class="field"><label for="facility-building">Tòa nhà</label><Select input-id="facility-building" v-model="form.buildingId" :options="buildings" option-label="name" option-value="id" show-clear placeholder="Khu tiện ích chung"><template #option="slot"><span>{{ slot.option.code }} · {{ slot.option.name }}</span></template></Select></div><div class="field"><label for="facility-location">Vị trí</label><InputText id="facility-location" v-model.trim="form.location" placeholder="Ví dụ: Tầng 2, tòa A2" /></div><div class="field"><label for="facility-capacity">Sức chứa</label><InputNumber input-id="facility-capacity" v-model="form.capacity" :min="1" suffix=" người" /></div><div class="field"><label for="facility-cover">Đường dẫn ảnh bìa</label><InputText id="facility-cover" v-model.trim="form.coverImageUrl" placeholder="/facilities/gym.svg" /></div></div></div><div class="form-section"><h3>Vận hành</h3><div class="form-grid"><div class="field"><label>Loại</label><Select v-model="form.type" :options="types" :option-label="labelFor" /></div><div class="field"><label>Trạng thái</label><Select v-model="form.status" :options="statuses" :option-label="labelFor" /></div><div class="field"><label>Giờ mở cửa</label><InputText v-model="form.openingTime" placeholder="06:00" /></div><div class="field"><label>Giờ đóng cửa</label><InputText v-model="form.closingTime" placeholder="22:00" /></div><div class="field"><label>Cho phép đặt lịch</label><ToggleSwitch v-model="form.bookable" /></div><div class="field"><label>Đang hoạt động</label><ToggleSwitch v-model="form.active" /></div></div></div><template #footer><Button label="Hủy" text @click="visible = false" /><Button label="Lưu tiện ích" icon="pi pi-check" :loading="submitting" @click="save" /></template></Dialog>
</template>
