import { labelFor } from './status'

const icons = {
  GYM: 'pi pi-heart', BADMINTON_COURT: 'pi pi-circle', COMMUNITY_ROOM: 'pi pi-users',
  READING_ROOM: 'pi pi-book', MEETING_ROOM: 'pi pi-comments'
}
const covers = {
  GYM: '/facilities/gym.svg', BADMINTON_COURT: '/facilities/badminton.svg',
  COMMUNITY_ROOM: '/facilities/community.svg', READING_ROOM: '/facilities/reading.svg',
  MEETING_ROOM: '/facilities/meeting.svg', OTHER: '/facilities/community.svg'
}
export const facilityIcon = (type) => icons[type] || 'pi pi-th-large'
export const facilityCover = (facility) => facility?.coverImageUrl || covers[facility?.type] || covers.OTHER
export const facilityTypeLabel = (type) => labelFor(type)
export const isBookable = (facility) => facility?.active && facility?.bookable && facility?.status === 'AVAILABLE'
