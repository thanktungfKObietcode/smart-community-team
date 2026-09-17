<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Message from 'primevue/message'
import { useAuth } from '../../composables/useAuth'
import { errorMessage } from '../../services/api'

const router = useRouter()
const { login, defaultRoute } = useAuth()
const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const submit = async () => {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = 'Vui lòng nhập email và mật khẩu.'
    return
  }
  loading.value = true
  try {
    const user = await login(email.value, password.value)
    router.push(defaultRoute(user.roles))
  } catch (exception) {
    error.value = errorMessage(exception, 'Email hoặc mật khẩu không chính xác.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <section class="login-hero">
      <span class="brand-mark"><i class="pi pi-building" /></span>
      <h1>Một cộng đồng vận hành thông minh hơn.</h1>
      <p>Quản lý yêu cầu, tiện ích, khách thăm và công việc hằng ngày trong một trải nghiệm rõ ràng, an toàn.</p>
      <div class="login-features">
        <span><i class="pi pi-check-circle" /> Dữ liệu theo đúng vai trò của bạn</span>
        <span><i class="pi pi-check-circle" /> Theo dõi dịch vụ và SLA minh bạch</span>
        <span><i class="pi pi-check-circle" /> Tối ưu cho cả máy tính và điện thoại</span>
      </div>
    </section>
    <section class="login-panel">
      <form class="login-form" @submit.prevent="submit">
        <h2>Chào mừng trở lại</h2>
        <p>Đăng nhập để tiếp tục tới Smart Community.</p>
        <Message v-if="error" severity="error" class="login-error">{{ error }}</Message>
        <div class="field"><label for="email">Email</label><InputText id="email" v-model.trim="email" type="email" autocomplete="email" placeholder="name@example.com" /></div>
        <div class="field"><label for="password">Mật khẩu</label><Password id="password" v-model="password" toggle-mask :feedback="false" autocomplete="current-password" placeholder="Nhập mật khẩu" fluid /></div>
        <Button type="submit" label="Đăng nhập" icon="pi pi-arrow-right" icon-pos="right" :loading="loading" fluid />
        <div class="login-demo"><strong>Tài khoản demo</strong><br />resident@test.com · manager@test.com · technician@test.com · security@test.com<br />Xem README của dự án để lấy thông tin đăng nhập.</div>
      </form>
    </section>
  </div>
</template>
