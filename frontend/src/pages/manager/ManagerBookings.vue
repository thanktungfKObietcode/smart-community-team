<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import api, { errorMessage } from '../../services/api'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import StatusTag from '../../components/StatusTag.vue'
import { formatDate } from '../../utils/status'

const bookings = ref([]); const loading = ref(true); const error = ref(''); const status = ref(null); const facilityId = ref(null)
const statuses = ['CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW']
const load = async () => { loading.value = true; error.value = ''; try { const params = {}; if (status.value) params.status = status.value; if (facilityId.value) params.facilityId = facilityId.value; bookings.value = (await api.get('/management/bookings', { params })).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false } }
onMounted(load)
</script>

<template><div class="page-heading"><div><h1>Đặt tiện ích</h1><p>Theo dõi lịch đặt của cư dân.</p></div></div><div class="filter-row"><Select v-model="status" :options="statuses" placeholder="Tất cả trạng thái" show-clear /><InputNumber v-model="facilityId" :use-grouping="false" placeholder="Facility ID" /><Button label="Lọc" icon="pi pi-filter" @click="load" /></div><div v-if="error" class="error-panel">{{ error }}</div><LoadingState v-else-if="loading" /><EmptyState v-else-if="!bookings.length" title="Không có lịch đặt" /><div v-else class="content-card table-card"><DataTable :value="bookings" responsive-layout="scroll"><Column field="code" header="Mã đặt chỗ" /><Column field="residentId" header="Resident ID" /><Column field="facilityName" header="Tiện ích" /><Column header="Bắt đầu"><template #body="{ data }">{{ formatDate(data.startTime) }}</template></Column><Column header="Kết thúc"><template #body="{ data }">{{ formatDate(data.endTime) }}</template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column></DataTable></div></template>
