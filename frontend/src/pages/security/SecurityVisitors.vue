<script setup>
import { ref } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import api, { errorMessage } from '../../services/api'
import StatusTag from '../../components/StatusTag.vue'
import { formatDate } from '../../utils/status'

const code = ref(''); const pass = ref(null); const loading = ref(false); const actionLoading = ref(false); const error = ref('')
const lookup = async () => { error.value = ''; pass.value = null; if (!code.value.trim()) { error.value = 'Nhập mã thẻ khách.'; return }; loading.value = true; try { pass.value = (await api.get(`/security/visitor-passes/${encodeURIComponent(code.value.trim())}`)).data } catch (exception) { error.value = errorMessage(exception) } finally { loading.value = false } }
const action = async (name) => { actionLoading.value = true; error.value = ''; try { pass.value = (await api.post(`/security/visitor-passes/${encodeURIComponent(pass.value.code)}/${name}`)).data } catch (exception) { error.value = errorMessage(exception) } finally { actionLoading.value = false } }
</script>

<template><div class="gate-page"><section class="gate-search"><span class="brand-mark"><i class="pi pi-shield" /></span><h1>Kiểm soát khách</h1><p>Tra cứu và xác nhận thẻ khách tại cổng ra vào.</p><form class="gate-form" @submit.prevent="lookup"><InputText v-model.trim="code" autofocus placeholder="Nhập mã, ví dụ VP-A82K91" /><Button type="submit" label="Tra cứu" icon="pi pi-search" :loading="loading" /></form><Message v-if="error" severity="error" class="page-message">{{ error }}</Message></section><article v-if="pass" class="visitor-verify-card"><div class="card-title"><div><span class="muted">Visitor pass</span><h2>{{ pass.code }}</h2></div><StatusTag :value="pass.status" /></div><div class="verify-name">{{ pass.visitorName }}</div><div class="verify-grid"><span><i class="pi pi-home" /> {{ pass.apartment?.buildingCode }} · {{ pass.apartment?.unitNumber }}</span><span><i class="pi pi-clock" /> {{ formatDate(pass.validFrom) }} – {{ formatDate(pass.validUntil) }}</span><span v-if="pass.visitorPhone"><i class="pi pi-phone" /> {{ pass.visitorPhone }}</span></div><div class="gate-actions"><Button v-if="pass.status === 'ACTIVE'" label="Check-in" icon="pi pi-sign-in" :loading="actionLoading" @click="action('check-in')" /><Button v-if="pass.status === 'CHECKED_IN'" label="Check-out" icon="pi pi-sign-out" severity="secondary" :loading="actionLoading" @click="action('check-out')" /></div></article></div></template>
