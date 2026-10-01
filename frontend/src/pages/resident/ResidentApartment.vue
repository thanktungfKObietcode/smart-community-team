<script setup>
import { onMounted, ref } from 'vue'
import LoadingState from '../../components/LoadingState.vue'
import PageHeader from '../../components/PageHeader.vue'
import api, { errorMessage } from '../../services/api'
import { labelFor } from '../../utils/status'
const resident = ref(null); const error = ref('')
onMounted(async () => { try { resident.value = (await api.get('/residents/me')).data } catch (e) { error.value = errorMessage(e) } })
</script>
<template>
  <PageHeader title="Thông tin căn hộ" description="Thông tin cư trú của bạn tại Smart Community." />
  <LoadingState v-if="!resident && !error" /><div v-else-if="error" class="error-panel">{{ error }}</div>
  <section v-else class="content-card resident-home-card"><span class="facility-icon"><i class="pi pi-home" /></span><div><p class="eyebrow">Căn hộ của tôi</p><h2>{{ resident.apartment?.unitNumber }} · Tòa {{ resident.apartment?.buildingCode }}</h2><p>{{ resident.fullName }} · {{ labelFor(resident.residentType) }}</p><div class="card-meta"><span><i class="pi pi-building" /> {{ resident.apartment?.buildingName }}</span><span><i class="pi pi-hashtag" /> Tầng {{ resident.apartment?.floorNumber }}</span></div></div></section>
</template>
