<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { useToast } from 'primevue/usetoast'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'

const toast = useToast(); const requests = ref([]); const loading = ref(true); const error = ref(''); const query = ref(''); const status = ref(null); const dialog = ref(false); const saving = ref(false); const formError = ref('')
const form = ref({ title: '', category: 'OTHER', description: '', priority: 'NORMAL' })
const statuses = ['OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED']; const categories = ['ELECTRICAL', 'PLUMBING', 'ELEVATOR', 'CLEANING', 'OTHER']
const filtered = computed(() => requests.value.filter((item) => (!status.value || item.status === status.value) && `${item.code} ${item.title} ${item.category}`.toLowerCase().includes(query.value.toLowerCase())))
const load = async () => { loading.value = true; error.value = ''; try { requests.value = (await api.get('/service-requests/my')).data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
const openCreate = () => { form.value = { title: '', category: 'OTHER', description: '', priority: 'NORMAL' }; formError.value = ''; dialog.value = true }
const create = async () => { if (!form.value.title || !form.value.description) { formError.value = 'Vui lòng cho biết vấn đề bạn đang gặp và mô tả chi tiết.'; return }; saving.value = true; formError.value = ''; try { const { data } = await api.post('/service-requests', form.value); dialog.value = false; toast.add({ severity: 'success', summary: 'Đã gửi báo sự cố', detail: `${data.code} đã được tiếp nhận. Ban quản lý sẽ phản hồi sớm.`, life: 4500 }); await load() } catch (e) { formError.value = errorMessage(e) } finally { saving.value = false } }
onMounted(load)
</script>
<template>
  <PageHeader title="Báo sự cố" description="Gửi yêu cầu hỗ trợ và theo dõi quá trình xử lý tại căn hộ."><Button label="Báo sự cố" icon="pi pi-plus" @click="openCreate" /></PageHeader>
  <section class="content-card"><div class="toolbar resident-filter"><InputText v-model="query" placeholder="Tìm theo mã hoặc nội dung" /><Select v-model="status" :options="statuses" :option-label="labelFor" placeholder="Tất cả trạng thái" show-clear /></div><LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div><EmptyState v-else-if="!filtered.length" icon="pi pi-wrench" title="Bạn chưa có yêu cầu hỗ trợ nào" text="Khi có sự cố tại căn hộ hoặc khu vực chung, hãy gửi báo sự cố để được hỗ trợ."><Button label="Báo sự cố" text @click="openCreate" /></EmptyState><div v-else class="request-card-list"><RouterLink v-for="item in filtered" :key="item.id" :to="`/resident/requests/${item.id}`" class="request-card"><div><span class="code">{{ item.code }}</span><h2>{{ item.title }}</h2><p>{{ labelFor(item.category) }} · Tạo lúc {{ formatDate(item.createdAt) }}</p></div><div class="request-card-side"><StatusTag :value="item.status" /><small :class="{ overdue: item.overdue }">Hạn xử lý: {{ formatDate(item.dueAt) }}</small></div></RouterLink></div></section>
  <Dialog v-model:visible="dialog" modal header="Bạn đang gặp vấn đề gì?" :style="{ width: 'min(40rem, 94vw)' }"><Message v-if="formError" severity="error">{{ formError }}</Message><div class="form-section"><h3>Thông tin sự cố</h3><div class="form-grid"><div class="field full"><label for="request-title">Tiêu đề <span class="required">*</span></label><InputText id="request-title" v-model.trim="form.title" maxlength="150" placeholder="Ví dụ: Rò rỉ nước tại bếp" /></div><div class="field full"><label for="request-category">Nhóm vấn đề <span class="required">*</span></label><Select input-id="request-category" v-model="form.category" :options="categories" :option-label="labelFor" /></div><div class="field full"><label for="request-description">Mô tả chi tiết <span class="required">*</span></label><Textarea id="request-description" v-model.trim="form.description" rows="5" maxlength="2000" placeholder="Vui lòng cho biết vị trí, thời điểm và mức độ ảnh hưởng..." /></div></div></div><p class="muted">Các yêu cầu từ cư dân được tiếp nhận ở mức ưu tiên bình thường. Với sự cố khẩn cấp, hãy liên hệ Ban quản lý ngay.</p><template #footer><Button label="Để sau" text @click="dialog = false" /><Button label="Gửi báo sự cố" icon="pi pi-send" :loading="saving" @click="create" /></template></Dialog>
</template>
