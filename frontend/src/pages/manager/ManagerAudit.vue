<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import api, { errorMessage } from '../../services/api'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import { formatDate } from '../../utils/status'

const logs = ref([]); const action = ref(''); const entityType = ref(''); const actorUserId = ref(null); const loading = ref(true); const error = ref('')
const load = async () => { loading.value = true; error.value = ''; try { const params = {}; if (action.value) params.action = action.value; if (entityType.value) params.entityType = entityType.value; if (actorUserId.value) params.actorUserId = actorUserId.value; logs.value = (await api.get('/management/audit-logs', { params })).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false } }
onMounted(load)
</script>

<template><div class="page-heading"><div><h1>Nhật ký kiểm toán</h1><p>Lịch sử các hoạt động nghiệp vụ quan trọng.</p></div></div><div class="filter-row"><InputText v-model.trim="action" placeholder="Hành động" /><InputText v-model.trim="entityType" placeholder="Loại đối tượng" /><InputNumber v-model="actorUserId" :use-grouping="false" placeholder="Actor user ID" /><Button label="Lọc" icon="pi pi-filter" @click="load" /></div><div v-if="error" class="error-panel">{{ error }}</div><LoadingState v-else-if="loading" /><EmptyState v-else-if="!logs.length" title="Không có nhật ký phù hợp" /><div v-else class="content-card table-card"><DataTable :value="logs" responsive-layout="scroll"><Column header="Thời điểm"><template #body="{ data }">{{ formatDate(data.timestamp) }}</template></Column><Column header="Người thực hiện"><template #body="{ data }">{{ data.actorFullName || 'Hệ thống' }}</template></Column><Column field="action" header="Hành động" /><Column header="Đối tượng"><template #body="{ data }">{{ data.entityType }} #{{ data.entityId || '—' }}</template></Column><Column field="description" header="Mô tả" /></DataTable></div></template>
