<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import StatCard from '../../components/StatCard.vue'
import LoadingState from '../../components/LoadingState.vue'
import api, { errorMessage } from '../../services/api'

const dashboard = ref(null); const error = ref('')
const load = async () => { error.value = ''; try { dashboard.value = (await api.get('/resident/dashboard')).data } catch (e) { error.value = errorMessage(e) } }
onMounted(load)
</script>
<template>
  <div class="page-heading"><div><h1>Chào {{ dashboard?.resident?.fullName || 'bạn' }}</h1><p>{{ dashboard?.resident?.apartment ? `Căn ${dashboard.resident.apartment} · Tòa ${dashboard.resident.building}` : 'Theo dõi không gian sống của bạn' }}</p></div></div>
  <div v-if="error" class="error-panel">{{ error }} <Button label="Thử lại" text @click="load" /></div><LoadingState v-else-if="!dashboard" />
  <template v-else><section class="kpi-grid"><StatCard label="Yêu cầu mới" :value="dashboard.serviceRequests.open" icon="pi pi-wrench" tone="teal" /><StatCard label="Đang xử lý" :value="dashboard.serviceRequests.inProgress" icon="pi pi-spin pi-cog" tone="amber" /><StatCard label="Lịch đặt sắp tới" :value="dashboard.bookings.upcoming" icon="pi pi-calendar" tone="blue" /><StatCard label="Khách còn hiệu lực" :value="dashboard.visitorPasses.active" icon="pi pi-users" tone="violet" /></section><section class="content-card" style="margin-top:1rem"><div class="page-heading"><div><h2 class="section-title" style="margin:0">Thao tác nhanh</h2><p>Thực hiện các tác vụ thường dùng ngay tại đây.</p></div></div><div class="card-grid quick-actions"><RouterLink to="/resident/requests"><Button label="Báo sự cố" icon="pi pi-wrench" outlined fluid /></RouterLink><RouterLink to="/resident/bookings"><Button label="Đặt tiện ích" icon="pi pi-calendar" outlined fluid /></RouterLink><RouterLink to="/resident/visitors"><Button label="Đăng ký khách" icon="pi pi-user-plus" outlined fluid /></RouterLink><RouterLink to="/resident/notifications"><Button :label="`Thông báo (${dashboard.unreadNotifications})`" icon="pi pi-bell" outlined fluid /></RouterLink></div></section></template>
</template>
