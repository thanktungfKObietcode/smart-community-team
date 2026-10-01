<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate } from '../../utils/status'

const route = useRoute(); const confirm = useConfirm(); const toast = useToast()
const bookings = ref([]); const loading = ref(true); const error = ref('')
const upcoming = computed(() => bookings.value.filter((item) => item.status === 'CONFIRMED' && new Date(item.endTime) >= new Date()))
const history = computed(() => bookings.value.filter((item) => !upcoming.value.includes(item)))
const load = async () => { loading.value = true; error.value = ''; try { bookings.value = (await api.get('/bookings/my')).data } catch (e) { error.value = errorMessage(e) } finally { loading.value = false } }
const cancel = (booking) => confirm.require({
  header: 'Hủy lịch đặt', message: `Bạn có chắc muốn hủy lịch ${booking.code} không?`, acceptLabel: 'Hủy lịch', rejectLabel: 'Quay lại', acceptClass: 'p-button-danger',
  accept: async () => { try { await api.patch(`/bookings/${booking.id}/cancel`); toast.add({ severity: 'success', summary: 'Đã hủy lịch', detail: 'Lịch đặt của bạn đã được hủy.', life: 3000 }); await load() } catch (e) { error.value = errorMessage(e) } }
})
onMounted(async () => { await load(); if (route.query.created) toast.add({ severity: 'success', summary: 'Đặt lịch thành công', detail: 'Lịch đặt của bạn đã được xác nhận.', life: 4000 }) })
</script>
<template>
  <PageHeader title="Lịch đặt của tôi" description="Theo dõi các lịch sử dụng tiện ích sắp tới và trước đây."><RouterLink to="/resident/facilities"><Button label="Khám phá tiện ích" icon="pi pi-th-large" /></RouterLink></PageHeader>
  <LoadingState v-if="loading" /><div v-else-if="error" class="error-panel">{{ error }}</div>
  <template v-else><section><h2 class="section-title">Sắp tới</h2><EmptyState v-if="!upcoming.length" icon="pi pi-calendar" title="Bạn chưa đặt tiện ích nào" text="Khám phá các tiện ích chung để chọn lịch phù hợp."><RouterLink to="/resident/facilities"><Button label="Khám phá tiện ích" text /></RouterLink></EmptyState><div v-else class="booking-card-grid"><article v-for="booking in upcoming" :key="booking.id" class="booking-card"><div class="visitor-top"><strong class="code">{{ booking.code }}</strong><StatusTag :value="booking.status" /></div><h3>{{ booking.facilityName }}</h3><div class="card-meta"><span><i class="pi pi-calendar" /> {{ formatDate(booking.startTime) }}</span><span><i class="pi pi-clock" /> Kết thúc {{ formatDate(booking.endTime) }}</span></div><Button label="Hủy lịch" severity="danger" text icon="pi pi-times" @click="cancel(booking)" /></article></div></section><section v-if="history.length"><h2 class="section-title">Lịch sử đặt tiện ích</h2><div class="booking-card-grid"><article v-for="booking in history" :key="booking.id" class="booking-card muted-card"><div class="visitor-top"><strong class="code">{{ booking.code }}</strong><StatusTag :value="booking.status" /></div><h3>{{ booking.facilityName }}</h3><span class="muted">{{ formatDate(booking.startTime) }}</span></article></div></section></template>
</template>
