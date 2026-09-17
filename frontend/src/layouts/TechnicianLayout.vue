<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [{ label: 'Tổng quan', icon: 'pi pi-home', to: '/technician/dashboard' }, { label: 'Công việc của tôi', icon: 'pi pi-wrench', to: '/technician/tasks' }]
</script><template><PortalLayout portal="Cổng kỹ thuật" :items="items" /></template>
