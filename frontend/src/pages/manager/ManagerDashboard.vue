<script setup>
import { computed, onMounted, ref } from 'vue'
import Chart from 'primevue/chart'
import StatCard from '../../components/StatCard.vue'
import LoadingState from '../../components/LoadingState.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate } from '../../utils/status'

const data = ref(null)
const error = ref('')
const chartOptions = { plugins: { legend: { position: 'bottom' } }, maintainAspectRatio: false }
const statusChart = computed(() => !data.value ? null : ({
  labels: ['Mới', 'Phân công', 'Đang xử lý', 'Đã xử lý', 'Hoàn tất'],
  datasets: [{ data: [data.value.serviceRequests.open, data.value.serviceRequests.assigned, data.value.serviceRequests.inProgress, data.value.serviceRequests.resolved, data.value.serviceRequests.closed], backgroundColor: ['#60a5fa', '#fbbf24', '#f97316', '#34d399', '#14b8a6'], borderWidth: 0 }]
}))
const categoryChart = computed(() => !data.value ? null : ({
  labels: Object.keys(data.value.serviceRequests.requestsByCategory || {}),
  datasets: [{ label: 'Yêu cầu', data: Object.values(data.value.serviceRequests.requestsByCategory || {}), backgroundColor: '#0f766e', borderRadius: 6 }]
}))
const load = async () => { try { data.value = (await api.get('/management/dashboard')).data } catch (exception) { error.value = errorMessage(exception) } }
onMounted(load)
</script>

<template>
  <div class="page-heading"><div><h1>Bảng điều khiển vận hành</h1><p>Tổng quan tình hình cộng đồng theo dữ liệu thời gian thực.</p></div></div>
  <div v-if="error" class="error-panel">{{ error }}</div>
  <LoadingState v-else-if="!data" />
  <template v-else>
    <section class="kpi-grid">
      <StatCard label="Tổng yêu cầu" :value="data.serviceRequests.total" icon="pi pi-wrench" tone="teal" /><StatCard label="Yêu cầu mở" :value="data.serviceRequests.open" icon="pi pi-inbox" tone="blue" /><StatCard label="Đang xử lý" :value="data.serviceRequests.inProgress" icon="pi pi-cog" tone="amber" /><StatCard label="Quá hạn SLA" :value="data.serviceRequests.overdue" icon="pi pi-exclamation-triangle" tone="rose" /><StatCard label="Lịch đặt hôm nay" :value="data.bookings.today" icon="pi pi-calendar" tone="violet" /><StatCard label="Khách hiện diện" :value="data.visitors.activeToday" icon="pi pi-users" tone="teal" />
    </section>
    <section class="dashboard-grid"><article class="content-card"><h2 class="section-title">Trạng thái yêu cầu</h2><div class="chart-wrap"><Chart type="doughnut" :data="statusChart" :options="chartOptions" /></div></article><article class="content-card"><h2 class="section-title">Yêu cầu theo danh mục</h2><div class="chart-wrap"><Chart type="bar" :data="categoryChart" :options="chartOptions" /></div></article></section>
    <section class="dashboard-grid"><article class="content-card"><h2 class="section-title">Hoạt động gần đây</h2><div v-if="!data.recentActivity?.length" class="muted">Chưa có hoạt động.</div><div v-else class="activity-list"><div v-for="item in data.recentActivity" :key="`${item.entityType}-${item.entityId}-${item.timestamp}`" class="activity"><strong>{{ item.action.replaceAll('_', ' ') }}</strong><small>{{ item.actorFullName || 'Hệ thống' }} · {{ item.description }} · {{ formatDate(item.timestamp) }}</small></div></div></article><article class="content-card"><h2 class="section-title">Chỉ số khác</h2><div class="card-meta"><span><i class="pi pi-users" /> {{ data.residents.active }} cư dân đang hoạt động</span><span><i class="pi pi-th-large" /> {{ data.facilities.available }}/{{ data.facilities.total }} tiện ích sẵn sàng</span><span><i class="pi pi-clock" /> Trung bình {{ data.serviceRequests.averageResolutionMinutes }} phút xử lý</span></div></article></section>
  </template>
</template>
