<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [
  { label: 'Bảng điều khiển', icon: 'pi pi-chart-bar', to: '/manager/dashboard' },
  { label: 'Yêu cầu dịch vụ', icon: 'pi pi-wrench', to: '/manager/requests' },
  { label: 'Cư dân', icon: 'pi pi-users', to: '/manager/residents' },
  { label: 'Tòa nhà & căn hộ', icon: 'pi pi-building', to: '/manager/buildings' },
  { label: 'Tiện ích', icon: 'pi pi-th-large', to: '/manager/facilities' },
  { label: 'Đặt chỗ', icon: 'pi pi-calendar', to: '/manager/bookings' },
  { label: 'Nhật ký vận hành', icon: 'pi pi-history', to: '/manager/audit' }
]
</script><template><PortalLayout portal="Cổng quản lý" :items="items" /></template>
