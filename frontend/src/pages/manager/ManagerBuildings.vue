<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import api, { errorMessage } from '../../services/api'
import LoadingState from '../../components/LoadingState.vue'
import EmptyState from '../../components/EmptyState.vue'

const buildings = ref([])
const apartments = ref([])
const selectedBuilding = ref(null)
const loading = ref(true)
const buildingDialog = ref(false)
const apartmentDialog = ref(false)
const apartmentsDialog = ref(false)
const submitting = ref(false)
const error = ref('')
const building = ref({ code: '', name: '', address: '' })
const apartment = ref({ unitNumber: '', floorNumber: null })

const load = async () => {
  loading.value = true
  try { buildings.value = (await api.get('/buildings')).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false }
}
const showApartments = async (item) => {
  error.value = ''
  selectedBuilding.value = item
  apartmentsDialog.value = true
  try { apartments.value = (await api.get(`/buildings/${item.id}/apartments`)).data } catch (exception) { error.value = errorMessage(exception) }
}
const saveBuilding = async () => {
  error.value = ''
  if (!building.value.code || !building.value.name) { error.value = 'Mã và tên tòa nhà là bắt buộc.'; return }
  submitting.value = true
  try { await api.post('/management/buildings', building.value); buildingDialog.value = false; building.value = { code: '', name: '', address: '' }; await load() } catch (exception) { error.value = errorMessage(exception) } finally { submitting.value = false }
}
const saveApartment = async () => {
  error.value = ''
  if (!apartment.value.unitNumber || !apartment.value.floorNumber) { error.value = 'Số căn và tầng là bắt buộc.'; return }
  submitting.value = true
  try { await api.post(`/management/buildings/${selectedBuilding.value.id}/apartments`, apartment.value); apartmentDialog.value = false; apartment.value = { unitNumber: '', floorNumber: null }; await showApartments(selectedBuilding.value) } catch (exception) { error.value = errorMessage(exception) } finally { submitting.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Tòa nhà & căn hộ</h1><p>Quản lý danh mục bất động sản của cộng đồng.</p></div><Button label="Tạo tòa nhà" icon="pi pi-plus" @click="buildingDialog = true" /></div>
  <Message v-if="error" severity="error" class="page-message">{{ error }}</Message>
  <LoadingState v-if="loading" />
  <EmptyState v-else-if="!buildings.length" title="Chưa có tòa nhà" description="Tạo tòa nhà đầu tiên để bắt đầu quản lý căn hộ." />
  <div v-else class="content-card table-card"><DataTable :value="buildings" data-key="id" responsive-layout="scroll"><Column field="code" header="Mã" /><Column field="name" header="Tên tòa nhà" /><Column field="address" header="Địa chỉ" /><Column header="Căn hộ"><template #body="{ data }"><Button label="Xem căn hộ" text size="small" @click="showApartments(data)" /></template></Column></DataTable></div>

  <Dialog v-model:visible="buildingDialog" modal header="Tạo tòa nhà" :style="{ width: 'min(32rem, 94vw)' }"><div class="form-grid"><div class="field"><label>Mã tòa nhà</label><InputText v-model.trim="building.code" placeholder="A2" /></div><div class="field"><label>Tên tòa nhà</label><InputText v-model.trim="building.name" placeholder="Tòa A2" /></div><div class="field field-full"><label>Địa chỉ</label><InputText v-model.trim="building.address" placeholder="Green City Residence" /></div></div><template #footer><Button label="Hủy" text @click="buildingDialog = false" /><Button label="Lưu" :loading="submitting" @click="saveBuilding" /></template></Dialog>
  <Dialog v-model:visible="apartmentsDialog" modal :header="`Căn hộ · ${selectedBuilding?.name || ''}`" :style="{ width: 'min(48rem, 94vw)' }" @hide="selectedBuilding = null"><template #header><div class="dialog-heading"><span>Căn hộ · {{ selectedBuilding?.name }}</span><Button label="Thêm căn hộ" icon="pi pi-plus" size="small" @click="apartmentDialog = true" /></div></template><EmptyState v-if="!apartments.length" title="Chưa có căn hộ" /><DataTable v-else :value="apartments" responsive-layout="scroll"><Column field="unitNumber" header="Số căn" /><Column field="floorNumber" header="Tầng" /><Column field="active" header="Trạng thái"><template #body="{ data }">{{ data.active ? 'Hoạt động' : 'Ngừng' }}</template></Column></DataTable></Dialog>
  <Dialog v-model:visible="apartmentDialog" modal header="Tạo căn hộ" :style="{ width: 'min(28rem, 94vw)' }"><div class="form-grid"><div class="field"><label>Số căn</label><InputText v-model.trim="apartment.unitNumber" placeholder="A1205" /></div><div class="field"><label>Tầng</label><InputNumber v-model="apartment.floorNumber" :min="1" /></div></div><template #footer><Button label="Hủy" text @click="apartmentDialog = false" /><Button label="Lưu" :loading="submitting" @click="saveApartment" /></template></Dialog>
</template>
