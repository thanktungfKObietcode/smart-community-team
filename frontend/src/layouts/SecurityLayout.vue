<script setup>
import PortalLayout from './PortalLayout.vue'
const items = [{ label: 'Xác thực khách', icon: 'pi pi-shield', to: '/security/visitors' }]
</script><template><PortalLayout portal="Cổng bảo vệ" :items="items" /></template>
