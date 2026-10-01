<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate } from '../../utils/status'

const confirm = useConfirm(); const toast = useToast(); const passes = ref([]); const loading = ref(true); const error = ref(''); const dialog = ref(false); const saving = ref(false); const formError = ref('')
const form = ref({ visitorName: '', visitorPhone: '', validFrom: null, validUntil: null })
const local = (value) => { const date = new Date(value); const two = (n) => String(n).padStart(2, '0'); return `${date.getFullYear()}-${two(date.getMonth() + 1)}-${two(date.getDate())}T${two(date.getHours())}:${two(date.getMinutes())}:00` }
const load = async () => { loading.value = true; error.value = ''; try { passes.value = (await api.get('/visitor-passes/my')).data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
const open = () => { form.value = { visitorName: '', visitorPhone: '', validFrom: null, validUntil: null }; formError.value = ''; dialog.value = true }
const save = async () => { if (!form.value.visitorName || !form.value.validFrom || !form.value.validUntil) { formError.value = 'Vui lòng nhập tên khách và thời gian dự kiến.'; return }; if (form.value.validFrom >= form.value.validUntil) { formError.value = 'Thời gian kết thúc phải sau thời gian bắt đầu.'; return }; saving.value = true; try { const { data } = await api.post('/visitor-passes', { visitorName: form.value.visitorName, visitorPhone: form.value.visitorPhone || null, validFrom: local(form.value.validFrom), validUntil: local(form.value.validUntil) }); dialog.value = false; toast.add({ severity: 'success', summary: 'Đã tạo thẻ khách', detail: `Mã thẻ ${data.code} đã sẵn sàng.`, life: 4000 }); await load() } catch (e) { formError.value = errorMessage(e) } finally { saving.value = false } }
const cancel = (pass) => confirm.require({ header: 'Hủy lời mời khách', message: `Bạn có chắc muốn hủy thẻ ${pass.code} không?`, acceptLabel: 'Hủy thẻ', rejectLabel: 'Quay lại', acceptClass: 'p-button-danger', accept: async () => { try { await api.patch(`/visitor-passes/${pass.id}/cancel`); await load() } catch (e) { error.value = errorMessage(e) } } })
onMounted(load)
</script>
<template>
  <PageHeader title="Khách của tôi" description="Tạo thẻ khách trước khi người thân, bạn bè đến thăm."><Button label="Mời khách" icon="pi pi-user-plus" @click="open" /></PageHeader>
  <LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div><EmptyState v-else-if="!passes.length" icon="pi pi-users" title="Bạn chưa tạo lời mời khách nào" text="Tạo thẻ khách để bảo vệ xác nhận nhanh khi khách đến."><Button label="Mời khách" text @click="open" /></EmptyState>
  <section v-else class="visitor-pass-grid"><article v-for="pass in passes" :key="pass.id" class="visitor-pass-card"><div class="pass-code-block"><span>THẺ KHÁCH</span><strong>{{ pass.code }}</strong><i class="pi pi-qrcode" aria-hidden="true" /></div><div class="visitor-top"><div><h2>{{ pass.visitorName }}</h2><p>{{ pass.visitorPhone || 'Chưa cung cấp số điện thoại' }}</p></div><StatusTag :value="pass.status" /></div><div class="card-meta"><span><i class="pi pi-home" /> {{ pass.apartment?.unitNumber }} · Tòa {{ pass.apartment?.buildingCode }}</span><span><i class="pi pi-clock" /> Từ {{ formatDate(pass.validFrom) }}</span><span><i class="pi pi-clock" /> Đến {{ formatDate(pass.validUntil) }}</span></div><Button v-if="pass.status === 'ACTIVE'" label="Hủy lời mời" severity="danger" text icon="pi pi-times" @click="cancel(pass)" /></article></section>
  <Dialog v-model:visible="dialog" modal header="Mời khách đến căn hộ" :style="{ width: 'min(40rem, 94vw)' }"><Message v-if="formError" severity="error">{{ formError }}</Message><div class="form-section"><h3>Thông tin khách</h3><div class="form-grid"><div class="field full"><label for="visitor-name">Họ tên khách <span class="required">*</span></label><InputText id="visitor-name" v-model.trim="form.visitorName" placeholder="Ví dụ: Nguyễn Văn Minh" /></div><div class="field full"><label for="visitor-phone">Số điện thoại</label><InputText id="visitor-phone" v-model.trim="form.visitorPhone" placeholder="Ví dụ: 0912 345 678" /></div></div></div><div class="form-section"><h3>Thời gian dự kiến</h3><div class="form-grid"><div class="field"><label for="visitor-from">Khách đến từ <span class="required">*</span></label><DatePicker input-id="visitor-from" v-model="form.validFrom" show-time hour-format="24" :min-date="new Date()" /></div><div class="field"><label for="visitor-until">Đến <span class="required">*</span></label><DatePicker input-id="visitor-until" v-model="form.validUntil" show-time hour-format="24" :min-date="new Date()" /></div></div></div><p class="muted">Mỗi thẻ khách có hiệu lực tối đa 24 giờ.</p><template #footer><Button label="Để sau" text @click="dialog = false" /><Button label="Tạo thẻ khách" icon="pi pi-ticket" :loading="saving" @click="save" /></template></Dialog>
</template>
