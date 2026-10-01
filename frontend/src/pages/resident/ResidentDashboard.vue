<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { formatDate, labelFor } from '../../utils/status'
import { notificationMessage, notificationTitle } from '../../utils/notification'

const dashboard = ref(null); const requests = ref([]); const bookings = ref([]); const passes = ref([]); const notifications = ref([]); const error = ref('')
const upcomingBooking = computed(() => bookings.value.find((item) => item.status === 'CONFIRMED' && new Date(item.startTime) > new Date()))
const upcomingPass = computed(() => passes.value.find((item) => item.status === 'ACTIVE' && new Date(item.validUntil) >= new Date()))
const recentRequests = computed(() => requests.value.slice(0, 3)); const unread = computed(() => notifications.value.filter((item) => !item.read).slice(0, 3))
const load = async () => { error.value = ''; try { const [summary, requestList, bookingList, passList, noteList] = await Promise.all([api.get('/resident/dashboard'), api.get('/service-requests/my'), api.get('/bookings/my'), api.get('/visitor-passes/my'), api.get('/notifications/my')]); dashboard.value = summary.data; requests.value = requestList.data; bookings.value = bookingList.data; passes.value = passList.data; notifications.value = noteList.data } catch (e) { error.value = errorMessage(e) } }
onMounted(load)
</script>
<template>
  <PageHeader v-if="dashboard" :title="`Xin chào, ${dashboard.resident.fullName}`" :description="dashboard.resident.apartment ? `Căn hộ ${dashboard.resident.apartment} • Tòa ${dashboard.resident.building}` : 'Không gian sống của bạn tại Smart Community'" eyebrow="Cộng đồng của bạn" />
  <LoadingState v-else-if="!error" /><div v-else class="error-panel">{{ error }} <Button label="Thử lại" text @click="load" /></div>
  <template v-if="dashboard"><section class="resident-quick-actions"><RouterLink to="/resident/requests"><i class="pi pi-wrench" /><strong>Báo sự cố</strong><small>Gửi yêu cầu hỗ trợ</small></RouterLink><RouterLink to="/resident/facilities"><i class="pi pi-calendar-plus" /><strong>Đặt tiện ích</strong><small>Chọn khung giờ phù hợp</small></RouterLink><RouterLink to="/resident/visitors"><i class="pi pi-user-plus" /><strong>Mời khách</strong><small>Tạo thẻ khách nhanh</small></RouterLink><RouterLink to="/resident/notifications"><i class="pi pi-bell" /><strong>Thông báo</strong><small>{{ dashboard.unreadNotifications }} tin chưa đọc</small></RouterLink></section>
    <section class="dashboard-grid resident-dashboard-grid"><article class="content-card"><div class="section-heading"><div><p class="eyebrow">Sắp tới</p><h2>Hoạt động gần nhất</h2></div></div><div v-if="upcomingBooking" class="upcoming-item"><i class="pi pi-calendar" /><div><strong>{{ upcomingBooking.facilityName }}</strong><small>{{ formatDate(upcomingBooking.startTime) }}</small></div><RouterLink to="/resident/bookings"><Button label="Xem lịch" text size="small" /></RouterLink></div><div v-if="upcomingPass" class="upcoming-item"><i class="pi pi-users" /><div><strong>Khách: {{ upcomingPass.visitorName }}</strong><small>{{ formatDate(upcomingPass.validFrom) }} · {{ upcomingPass.code }}</small></div><RouterLink to="/resident/visitors"><Button label="Xem thẻ" text size="small" /></RouterLink></div><p v-if="!upcomingBooking && !upcomingPass" class="muted empty-inline">Bạn chưa có lịch đặt hoặc lời mời khách sắp tới.</p></article><article class="content-card"><div class="section-heading"><div><p class="eyebrow">Yêu cầu gần đây</p><h2>Theo dõi sự cố</h2></div><RouterLink to="/resident/requests"><Button label="Xem tất cả" text size="small" /></RouterLink></div><div v-if="recentRequests.length" class="compact-list"><RouterLink v-for="item in recentRequests" :key="item.id" :to="`/resident/requests/${item.id}`" class="compact-row"><span><strong>{{ item.code }}</strong><small>{{ labelFor(item.category) }} · {{ item.title }}</small></span><StatusTag :value="item.status" /></RouterLink></div><p v-else class="muted empty-inline">Bạn chưa có yêu cầu hỗ trợ nào.</p></article></section>
    <section class="content-card"><div class="section-heading"><div><p class="eyebrow">Thông báo mới</p><h2>Không bỏ lỡ cập nhật</h2></div><RouterLink to="/resident/notifications"><Button label="Xem tất cả" text size="small" /></RouterLink></div><div v-if="unread.length" class="compact-list"><RouterLink v-for="item in unread" :key="item.id" to="/resident/notifications" class="compact-row"><span><strong>{{ notificationTitle(item) }}</strong><small>{{ notificationMessage(item) }} · {{ formatDate(item.createdAt) }}</small></span><i class="pi pi-angle-right" /></RouterLink></div><p v-else class="muted empty-inline">Bạn đã xem hết thông báo.</p></section>
  </template>
</template>
