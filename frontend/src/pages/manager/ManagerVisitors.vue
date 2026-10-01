<script setup>
import { computed, onMounted, ref } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate } from '../../utils/status'
const passes = ref([]); const loading = ref(true); const error = ref(''); const query = ref('')
const shown = computed(() => passes.value.filter((item) => `${item.code} ${item.visitorName} ${item.apartment?.unitNumber}`.toLowerCase().includes(query.value.toLowerCase())))
const load = async () => { loading.value = true; try { passes.value = (await api.get('/management/visitor-passes')).data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
onMounted(load)
</script>
<template><PageHeader title="Khách ra vào" description="Theo dõi tình trạng các thẻ khách trong cộng đồng." /><section class="content-card"><div class="toolbar"><InputText v-model="query" placeholder="Tìm mã thẻ, tên khách hoặc căn hộ" /></div><LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div><EmptyState v-else-if="!shown.length" icon="pi pi-users" title="Chưa có thẻ khách phù hợp" text="Các lời mời khách từ cư dân sẽ xuất hiện tại đây." /><DataTable v-else :value="shown" paginator :rows="10" responsive-layout="scroll"><Column field="code" header="Mã thẻ" /><Column field="visitorName" header="Khách" /><Column header="Căn hộ"><template #body="{ data }">{{ data.apartment?.unitNumber }} · Tòa {{ data.apartment?.buildingCode }}</template></Column><Column header="Hiệu lực đến"><template #body="{ data }">{{ formatDate(data.validUntil) }}</template></Column><Column header="Trạng thái"><template #body="{ data }"><StatusTag :value="data.status" /></template></Column></DataTable></section></template>
