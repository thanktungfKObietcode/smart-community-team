<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Select from 'primevue/select'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'
const bookings = ref([]); const facilities = ref([]); const loading = ref(true); const error = ref(''); const status = ref(null); const facility = ref(null)
const statuses = ['CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW']
const load = async () => { loading.value = true; error.value = ''; try { const params = {}; if (status.value) params.status = status.value; if (facility.value) params.facilityId = facility.value.id; const [bookingsResponse, facilitiesResponse] = await Promise.all([api.get('/management/bookings', { params }), api.get('/facilities')]); bookings.value = bookingsResponse.data; facilities.value = facilitiesResponse.data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
onMounted(load)
</script>
<template><PageHeader title="Lịch đặt" description="Theo dõi việc sử dụng tiện ích của cư dân."><Button label="Lọc lịch đặt" icon="pi pi-filter" @click="load" /></PageHeader><div class="toolbar content-card"><Select v-model="status" :options="statuses" :option-label="labelFor" placeholder="Tất cả trạng thái" show-clear /><Select v-model="facility" :options="facilities" option-label="name" placeholder="Tất cả tiện ích" show-clear /></div><div v-if="error" class="error-panel">{{ error }}</div><LoadingState v-else-if="loading" /><EmptyState v-else-if="!bookings.length" icon="pi pi-calendar" title="Chưa có lịch đặt phù hợp" text="Các lịch đặt tiện ích của cư dân sẽ hiển thị tại đây." /><div v-else class="content-card table-card"><DataTable :value="bookings" responsive-layout="scroll"><Column field="code" header="Mã lịch đặt" /><Column field="facilityName" header="Tiện ích" /><Column header="Bắt đầu"><template #body="{ data }">{{ formatDate(data.startTime) }}</template></Column><Column header="Kết thúc"><template #body="{ data }">{{ formatDate(data.endTime) }}</template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column></DataTable></div></template>
