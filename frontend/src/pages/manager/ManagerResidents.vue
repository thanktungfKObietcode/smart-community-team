<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import DatePicker from 'primevue/datepicker'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Password from 'primevue/password'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { labelFor } from '../../utils/status'

const rows = ref([])
const buildings = ref([])
const apartments = ref([])
const selectedBuilding = ref(null)
const loading = ref(true)
const error = ref('')
const query = ref('')
const dialog = ref(false)
const saving = ref(false)
const formError = ref('')
const form = ref(emptyForm())
const residentTypes = ['OWNER', 'TENANT', 'FAMILY_MEMBER']
const residentTypeOptions = residentTypes.map((value) => ({ value, label: labelFor(value) }))

function emptyForm() {
  return { fullName: '', email: '', initialPassword: '', apartmentId: null, residentType: 'OWNER', phone: '', moveInDate: null }
}

const filtered = computed(() => rows.value.filter((resident) =>
  `${resident.fullName} ${resident.email} ${resident.apartment?.buildingCode} ${resident.apartment?.unitNumber}`
    .toLowerCase().includes(query.value.toLowerCase())
))

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [residents, buildingList] = await Promise.all([
      api.get('/management/residents'),
      api.get('/buildings')
    ])
    rows.value = residents.data
    buildings.value = buildingList.data
  } catch (exception) {
    error.value = errorMessage(exception)
  } finally {
    loading.value = false
  }
}

watch(selectedBuilding, async (building) => {
  apartments.value = []
  form.value.apartmentId = null
  if (!building) return
  try {
    apartments.value = (await api.get(`/buildings/${building.id}/apartments`)).data
  } catch (exception) {
    formError.value = errorMessage(exception)
  }
})

const open = () => {
  form.value = emptyForm()
  selectedBuilding.value = null
  apartments.value = []
  formError.value = ''
  dialog.value = true
}

const submit = async () => {
  if (!form.value.fullName || !form.value.email || !form.value.initialPassword || !form.value.apartmentId) {
    formError.value = 'Vui lòng nhập thông tin cư dân và chọn căn hộ.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await api.post('/management/residents', {
      ...form.value,
      moveInDate: form.value.moveInDate?.toISOString().slice(0, 10) || null
    })
    dialog.value = false
    await load()
  } catch (exception) {
    formError.value = errorMessage(exception)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Cư dân</h1><p>Thêm cư dân cùng tài khoản đăng nhập và căn hộ hợp lệ.</p></div><Button label="Thêm cư dân" icon="pi pi-user-plus" @click="open" /></div>
  <div class="content-card">
    <div class="toolbar"><InputText v-model="query" placeholder="Tìm cư dân..." /></div>
    <LoadingState v-if="loading" />
    <div v-else-if="error" class="error-panel">{{ error }}</div>
    <EmptyState v-else-if="!filtered.length" title="Chưa có cư dân" />
    <DataTable v-else :value="filtered" paginator :rows="10" class="data-table" responsive-layout="scroll">
      <Column field="fullName" header="Họ tên" /><Column field="email" header="Email" />
      <Column header="Căn hộ"><template #body="{ data }">{{ data.apartment?.buildingCode }} · {{ data.apartment?.unitNumber }}</template></Column>
      <Column field="phone" header="Điện thoại" /><Column header="Loại"><template #body="{ data }"><StatusTag :value="data.residentType" /></template></Column>
      <Column header="Trạng thái"><template #body="{ data }"><Tag :value="data.active ? 'Đang hoạt động' : 'Ngừng hoạt động'" :severity="data.active ? 'success' : 'secondary'" rounded /></template></Column>
    </DataTable>
  </div>

  <Dialog v-model:visible="dialog" modal header="Thêm cư dân" :style="{ width: 'min(42rem, 94vw)' }">
    <Message v-if="formError" severity="error">{{ formError }}</Message>
    <div class="form-grid">
      <div class="field full"><label>Họ tên</label><InputText v-model.trim="form.fullName" /></div>
      <div class="field"><label>Email</label><InputText v-model.trim="form.email" type="email" /></div>
      <div class="field"><label>Mật khẩu ban đầu</label><Password v-model="form.initialPassword" toggle-mask :feedback="false" fluid /></div>
      <div class="field"><label>Tòa nhà</label><Select v-model="selectedBuilding" :options="buildings" option-label="name" placeholder="Chọn tòa nhà"><template #option="{ option }">{{ option.code }} · {{ option.name }}</template></Select></div>
      <div class="field"><label>Căn hộ</label><Select v-model="form.apartmentId" :options="apartments" option-label="unitNumber" option-value="id" :disabled="!selectedBuilding" placeholder="Chọn căn hộ"><template #option="{ option }">{{ option.unitNumber }} · Tầng {{ option.floorNumber }}</template></Select></div>
      <div class="field"><label>Loại cư dân</label><Select v-model="form.residentType" :options="residentTypeOptions" option-label="label" option-value="value" placeholder="Chọn loại cư dân" /></div>
      <div class="field"><label>Ngày vào ở</label><DatePicker v-model="form.moveInDate" date-format="dd/mm/yy" /></div>
      <div class="field full"><label>Điện thoại</label><InputText v-model.trim="form.phone" /></div>
    </div>
    <div class="dialog-footer"><Button label="Hủy" text @click="dialog = false" /><Button label="Tạo cư dân" :loading="saving" @click="submit" /></div>
  </Dialog>
</template>
