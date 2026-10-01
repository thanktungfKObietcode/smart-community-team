<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import api, { errorMessage } from '../../services/api'
import { auditDescription, formatDate, labelFor } from '../../utils/status'

const logs = ref([]); const action = ref(null); const entityType = ref(null); const actorUserId = ref(''); const loading = ref(true); const error = ref('')
const actions = [
  { label: 'Tạo báo sự cố', value: 'SERVICE_REQUEST_CREATED' }, { label: 'Phân công sự cố', value: 'SERVICE_REQUEST_ASSIGNED' }, { label: 'Bắt đầu xử lý', value: 'SERVICE_REQUEST_STARTED' }, { label: 'Cập nhật kết quả', value: 'SERVICE_REQUEST_RESOLVED' }, { label: 'Xác nhận hoàn thành', value: 'SERVICE_REQUEST_CLOSED' },
  { label: 'Xác nhận lịch đặt', value: 'BOOKING_CONFIRMED' }, { label: 'Hủy lịch đặt', value: 'BOOKING_CANCELLED' }, { label: 'Tạo thẻ khách', value: 'VISITOR_PASS_CREATED' }, { label: 'Khách check-in', value: 'VISITOR_CHECKED_IN' }, { label: 'Khách check-out', value: 'VISITOR_CHECKED_OUT' }, { label: 'Hủy thẻ khách', value: 'VISITOR_CANCELLED' }, { label: 'Thêm tiện ích', value: 'FACILITY_CREATED' }, { label: 'Cập nhật tiện ích', value: 'FACILITY_UPDATED' }
]
const entityTypes = [{ label: 'Báo sự cố', value: 'SERVICE_REQUEST' }, { label: 'Lịch đặt', value: 'BOOKING' }, { label: 'Thẻ khách', value: 'VISITOR_PASS' }, { label: 'Tiện ích', value: 'FACILITY' }]
const load = async () => { loading.value = true; error.value = ''; try { const params = {}; if (action.value) params.action = action.value; if (entityType.value) params.entityType = entityType.value; if (actorUserId.value) params.actorUserId = actorUserId.value; logs.value = (await api.get('/management/audit-logs', { params })).data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
onMounted(load)
</script>
<template><PageHeader title="Nhật ký hoạt động" description="Theo dõi các thao tác nghiệp vụ quan trọng trong cộng đồng."><Button label="Lọc nhật ký" icon="pi pi-filter" @click="load" /></PageHeader><div class="toolbar content-card"><Select v-model="action" :options="actions" option-label="label" option-value="value" placeholder="Tất cả hoạt động" show-clear /><Select v-model="entityType" :options="entityTypes" option-label="label" option-value="value" placeholder="Tất cả loại dữ liệu" show-clear /><InputText v-model.trim="actorUserId" inputmode="numeric" placeholder="Mã người thực hiện (không bắt buộc)" /></div><div v-if="error" class="error-panel">{{ error }}</div><LoadingState v-else-if="loading" /><EmptyState v-else-if="!logs.length" icon="pi pi-history" title="Chưa có nhật ký phù hợp" text="Các hoạt động nghiệp vụ sẽ xuất hiện tại đây." /><div v-else class="content-card table-card"><DataTable :value="logs" responsive-layout="scroll"><Column header="Thời điểm"><template #body="{ data }">{{ formatDate(data.timestamp) }}</template></Column><Column header="Người thực hiện"><template #body="{ data }">{{ data.actorFullName || 'Hệ thống' }}</template></Column><Column header="Hoạt động"><template #body="{ data }">{{ labelFor(data.action) }}</template></Column><Column header="Dữ liệu liên quan"><template #body="{ data }">{{ labelFor(data.entityType) }}{{ data.entityId ? ` · Mã ${data.entityId}` : '' }}</template></Column><Column header="Mô tả"><template #body="{ data }">{{ auditDescription(data.action, data.description) }}</template></Column></DataTable></div></template>
