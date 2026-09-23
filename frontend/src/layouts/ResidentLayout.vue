<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [
  { label: 'Tổng quan', icon: 'pi pi-home', to: '/resident/dashboard' },
  { label: 'Yêu cầu dịch vụ', icon: 'pi pi-wrench', to: '/resident/requests' },
  { label: 'Đặt tiện ích', icon: 'pi pi-calendar', to: '/resident/bookings' },
  { label: 'Khách thăm', icon: 'pi pi-users', to: '/resident/visitors' },
  { label: 'Thông báo', icon: 'pi pi-bell', to: '/resident/notifications' }
]
</script><template><PortalLayout portal="Cổng cư dân" :items="items" /></template>
