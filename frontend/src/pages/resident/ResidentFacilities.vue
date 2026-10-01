<script setup>
import { computed, onMounted, ref } from 'vue'
import Button from 'primevue/button'
import Select from 'primevue/select'
import LoadingState from '../../components/LoadingState.vue'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import api, { errorMessage } from '../../services/api'
import { facilityCover, facilityIcon, isBookable } from '../../utils/facility'
import { formatTime, labelFor } from '../../utils/status'

const facilities = ref([])
const loading = ref(true)
const error = ref('')
const type = ref(null)
const types = computed(() => [...new Set(facilities.value.map((item) => item.type))])
const shown = computed(() => facilities.value.filter((item) => !type.value || item.type === type.value))
const load = async () => {
  loading.value = true; error.value = ''
  try { facilities.value = (await api.get('/facilities')).data }
  catch (exception) { error.value = errorMessage(exception) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <PageHeader title="Tiện ích" description="Khám phá không gian chung và đặt lịch theo nhu cầu của bạn.">
    <Select v-model="type" :options="types" :option-label="labelFor" placeholder="Tất cả loại tiện ích" show-clear class="filter-select" />
  </PageHeader>
  <LoadingState v-if="loading" />
  <div v-else-if="error" class="error-panel">{{ error }} <Button label="Thử lại" text @click="load" /></div>
  <EmptyState v-else-if="!shown.length" icon="pi pi-th-large" title="Chưa có tiện ích phù hợp" text="Hãy thử thay đổi bộ lọc hoặc quay lại sau." />
  <section v-else class="facility-showcase-grid">
    <article v-for="facility in shown" :key="facility.id" class="facility-showcase-card">
      <img :src="facilityCover(facility)" :alt="`Ảnh ${facility.name}`" class="facility-cover" @error="$event.target.style.display = 'none'" />
      <div class="facility-showcase-body">
        <div class="visitor-top"><span class="facility-icon"><i :class="facilityIcon(facility.type)" /></span><StatusTag :value="facility.status" /></div>
        <p class="eyebrow">{{ labelFor(facility.type) }}</p><h2>{{ facility.name }}</h2>
        <p class="facility-description">{{ facility.description || 'Không gian tiện ích phục vụ cư dân trong khu.' }}</p>
        <div class="card-meta"><span><i class="pi pi-map-marker" /> {{ facility.location || 'Khu tiện ích chung' }}</span><span><i class="pi pi-clock" /> {{ formatTime(facility.openingTime) }} – {{ formatTime(facility.closingTime) }}</span><span v-if="facility.capacity"><i class="pi pi-users" /> Tối đa {{ facility.capacity }} người</span></div>
        <RouterLink :to="`/resident/facilities/${facility.id}`"><Button :label="isBookable(facility) ? 'Xem chi tiết & đặt lịch' : 'Xem chi tiết'" :icon="isBookable(facility) ? 'pi pi-calendar-plus' : 'pi pi-arrow-right'" fluid /></RouterLink>
      </div>
    </article>
  </section>
</template>
