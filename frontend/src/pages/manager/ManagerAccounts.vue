<script setup>
import { onMounted, ref } from 'vue'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Password from 'primevue/password'
import Select from 'primevue/select'
import Tag from 'primevue/tag'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import EmptyState from '../../components/EmptyState.vue'
import LoadingState from '../../components/LoadingState.vue'
import api, { errorMessage } from '../../services/api'
import { labelFor } from '../../utils/status'

const accounts = ref([])
const loading = ref(true)
const error = ref('')
const dialog = ref(false)
const saving = ref(false)
const formError = ref('')
const form = ref({ fullName: '', email: '', initialPassword: '', role: null })
const roles = [
  { label: 'Ban quản lý', value: 'MANAGER' },
  { label: 'Kỹ thuật viên', value: 'TECHNICIAN' },
  { label: 'Bảo vệ', value: 'SECURITY' }
]
const toast = useToast()
const confirm = useConfirm()

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    accounts.value = (await api.get('/admin/users')).data
  } catch (exception) {
    error.value = errorMessage(exception)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  form.value = { fullName: '', email: '', initialPassword: '', role: null }
  formError.value = ''
  dialog.value = true
}

const submit = async () => {
  if (!form.value.fullName || !form.value.email || !form.value.initialPassword || !form.value.role) {
    formError.value = 'Vui lòng điền đầy đủ thông tin tài khoản.'
    return
  }
  saving.value = true
  formError.value = ''
  try {
    await api.post('/admin/users', form.value)
    dialog.value = false
    toast.add({ severity: 'success', summary: 'Đã tạo tài khoản', detail: 'Tài khoản nhân sự đã sẵn sàng sử dụng.', life: 3000 })
    await load()
  } catch (exception) {
    formError.value = errorMessage(exception)
  } finally {
    saving.value = false
  }
}

const changeStatus = (account) => {
  const action = account.active ? 'vô hiệu hóa' : 'kích hoạt'
  confirm.require({
    message: `Bạn có chắc muốn ${action} tài khoản ${account.fullName}?`,
    header: 'Xác nhận thay đổi trạng thái',
    icon: 'pi pi-exclamation-triangle',
    rejectLabel: 'Hủy',
    acceptLabel: action === 'vô hiệu hóa' ? 'Vô hiệu hóa' : 'Kích hoạt',
    accept: async () => {
      try {
        await api.patch(`/admin/users/${account.userId}/active`, { active: !account.active })
        toast.add({ severity: 'success', summary: 'Đã cập nhật trạng thái', life: 2500 })
        await load()
      } catch (exception) {
        toast.add({ severity: 'error', summary: 'Không thể cập nhật', detail: errorMessage(exception), life: 4000 })
      }
    }
  })
}

onMounted(load)
</script>

<template>
  <div class="page-heading">
    <div><h1>Quản lý tài khoản</h1><p>Tạo và quản lý tài khoản Ban quản lý, Kỹ thuật viên và Bảo vệ.</p></div>
    <Button label="Tạo tài khoản nhân sự" icon="pi pi-user-plus" @click="openCreate" />
  </div>

  <div class="content-card">
    <LoadingState v-if="loading" />
    <div v-else-if="error" class="error-panel">{{ error }}</div>
    <EmptyState v-else-if="!accounts.length" icon="pi pi-users" title="Chưa có tài khoản nhân sự" description="Tạo tài khoản đầu tiên để bắt đầu vận hành." />
    <DataTable v-else :value="accounts" paginator :rows="10" class="data-table" responsive-layout="scroll">
      <Column field="fullName" header="Họ tên" />
      <Column field="email" header="Email" />
      <Column header="Vai trò"><template #body="{ data }">{{ data.roles.map(labelFor).join(', ') }}</template></Column>
      <Column header="Trạng thái"><template #body="{ data }"><Tag :value="data.active ? 'Đang hoạt động' : 'Đã vô hiệu hóa'" :severity="data.active ? 'success' : 'secondary'" rounded /></template></Column>
      <Column header=""><template #body="{ data }"><Button :label="data.active ? 'Vô hiệu hóa' : 'Kích hoạt'" :severity="data.active ? 'danger' : 'success'" text @click="changeStatus(data)" /></template></Column>
    </DataTable>
  </div>

  <Dialog v-model:visible="dialog" modal header="Tạo tài khoản nhân sự" :style="{ width: 'min(34rem, 94vw)' }">
    <Message v-if="formError" severity="error">{{ formError }}</Message>
    <div class="form-grid">
      <div class="field full"><label for="staff-name">Họ tên</label><InputText id="staff-name" v-model.trim="form.fullName" /></div>
      <div class="field full"><label for="staff-email">Email</label><InputText id="staff-email" v-model.trim="form.email" type="email" /></div>
      <div class="field"><label for="staff-password">Mật khẩu ban đầu</label><Password id="staff-password" v-model="form.initialPassword" toggle-mask :feedback="false" fluid /></div>
      <div class="field"><label>Vai trò</label><Select v-model="form.role" :options="roles" option-label="label" option-value="value" placeholder="Chọn vai trò" /></div>
    </div>
    <div class="dialog-footer"><Button label="Hủy" text @click="dialog = false" /><Button label="Tạo tài khoản" :loading="saving" @click="submit" /></div>
  </Dialog>
</template>
