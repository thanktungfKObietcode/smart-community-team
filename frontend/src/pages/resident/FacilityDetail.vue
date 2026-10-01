<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import StatusTag from '../../components/StatusTag.vue'
import LoadingState from '../../components/LoadingState.vue'
import api, { errorMessage } from '../../services/api'
import { facilityCover, isBookable } from '../../utils/facility'
import { formatDateOnly, formatTime, labelFor } from '../../utils/status'

const route = useRoute(); const router = useRouter()
const facility = ref(null); const busySlots = ref([]); const selectedDate = ref(new Date()); const selectedSlot = ref(null)
const loading = ref(true); const slotsLoading = ref(false); const booking = ref(false); const error = ref(''); const formError = ref('')
const dateKey = (date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
const asLocal = (date, hours) => { const copy = new Date(date); copy.setHours(hours, 0, 0, 0); return copy }
const slots = computed(() => {
  if (!facility.value || !selectedDate.value) return []
  const opening = Number(String(facility.value.openingTime).slice(0, 2)); const closing = Number(String(facility.value.closingTime).slice(0, 2))
  const now = new Date()
  return Array.from({ length: Math.max(0, closing - opening) }, (_, index) => {
    const start = asLocal(selectedDate.value, opening + index); const end = asLocal(selectedDate.value, opening + index + 1)
    const unavailable = busySlots.value.some((item) => start < new Date(item.endTime) && end > new Date(item.startTime)) || start <= now
    return { start, end, unavailable, label: `${String(start.getHours()).padStart(2, '0')}:00 – ${String(end.getHours()).padStart(2, '0')}:00` }
  })
})
const loadSlots = async () => {
  if (!facility.value || !selectedDate.value) return
  slotsLoading.value = true; selectedSlot.value = null
  try { busySlots.value = (await api.get(`/bookings/facilities/${facility.value.id}/availability`, { params: { date: dateKey(selectedDate.value) } })).data }
  catch (exception) { formError.value = errorMessage(exception) }
  finally { slotsLoading.value = false }
}
const load = async () => {
  loading.value = true; error.value = ''
  try { facility.value = (await api.get(`/facilities/${route.params.id}`)).data; await loadSlots() }
  catch (exception) { error.value = errorMessage(exception) }
  finally { loading.value = false }
}
const reserve = async () => {
  if (!selectedSlot.value) { formError.value = 'Vui lòng chọn khung giờ phù hợp.'; return }
  booking.value = true; formError.value = ''
  const localDateTime = (value) => `${dateKey(value)}T${String(value.getHours()).padStart(2, '0')}:${String(value.getMinutes()).padStart(2, '0')}:00`
  try { await api.post('/bookings', { facilityId: facility.value.id, startTime: localDateTime(selectedSlot.value.start), endTime: localDateTime(selectedSlot.value.end) }); router.push('/resident/bookings?created=1') }
  catch (exception) { formError.value = errorMessage(exception) }
  finally { booking.value = false }
}
watch(selectedDate, loadSlots)
onMounted(load)
</script>

<template>
  <Button label="Quay lại tiện ích" icon="pi pi-arrow-left" text @click="router.push('/resident/facilities')" />
  <LoadingState v-if="loading" />
  <div v-else-if="error" class="error-panel">{{ error }}</div>
  <template v-else>
    <section class="facility-hero"><img :src="facilityCover(facility)" :alt="facility.name" @error="$event.target.style.display='none'" /><div class="facility-hero-content"><StatusTag :value="facility.status" /><p class="eyebrow">{{ labelFor(facility.type) }}</p><h1>{{ facility.name }}</h1><p>{{ facility.description || 'Không gian tiện ích dành cho cư dân.' }}</p><div class="hero-meta"><span><i class="pi pi-map-marker" /> {{ facility.location || 'Khu tiện ích chung' }}</span><span><i class="pi pi-clock" /> {{ formatTime(facility.openingTime) }} – {{ formatTime(facility.closingTime) }}</span><span v-if="facility.capacity"><i class="pi pi-users" /> Tối đa {{ facility.capacity }} người</span></div></div></section>
    <section v-if="facility.galleryImageUrls?.length" class="facility-gallery" aria-label="Ảnh tiện ích"><img v-for="image in facility.galleryImageUrls" :key="image" :src="image" :alt="`Không gian ${facility.name}`" @error="$event.target.style.display='none'" /></section>
    <section class="booking-planner content-card">
      <div><p class="eyebrow">Đặt lịch tiện ích</p><h2>Chọn ngày và khung giờ</h2><p class="muted">Các khung giờ mờ đã có lịch đặt hoặc đã qua. Hệ thống sẽ xác nhận lại khi bạn hoàn tất.</p></div>
      <Message v-if="formError" severity="error">{{ formError }}</Message>
      <div v-if="!isBookable(facility)" class="availability-note">Tiện ích này hiện chưa thể nhận lịch đặt.</div>
      <template v-else><div class="field date-field"><label for="booking-date">Ngày sử dụng</label><DatePicker input-id="booking-date" v-model="selectedDate" :min-date="new Date()" date-format="dd/mm/yy" /></div><div class="slot-list"><ProgressSpinner v-if="slotsLoading" style="width:2rem;height:2rem" /><button v-for="slot in slots" v-else :key="slot.label" class="time-slot" :class="{ selected: selectedSlot?.label === slot.label }" :disabled="slot.unavailable" @click="selectedSlot = slot">{{ slot.label }}</button></div><div class="booking-summary"><span>{{ selectedSlot ? `Đã chọn ${formatDateOnly(selectedDate)} · ${selectedSlot.label}` : 'Chưa chọn khung giờ' }}</span><Button label="Xác nhận đặt lịch" icon="pi pi-calendar-plus" :disabled="!selectedSlot" :loading="booking" @click="reserve" /></div></template>
    </section>
  </template>
</template>
