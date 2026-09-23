export const labels = {
  OPEN: 'Mới tạo', ASSIGNED: 'Đã phân công', IN_PROGRESS: 'Đang xử lý', RESOLVED: 'Đã xử lý', CLOSED: 'Hoàn tất', CANCELLED: 'Đã hủy',
  CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', NO_SHOW: 'Không đến',
  ACTIVE: 'Còn hiệu lực', CHECKED_IN: 'Đã vào', CHECKED_OUT: 'Đã ra', EXPIRED: 'Hết hạn',
  AVAILABLE: 'Hoạt động', MAINTENANCE: 'Bảo trì', OUT_OF_SERVICE: 'Ngừng hoạt động',
  ELECTRICAL: 'Điện', PLUMBING: 'Nước', ELEVATOR: 'Thang máy', CLEANING: 'Vệ sinh', OTHER: 'Khác',
  BADMINTON_COURT: 'Sân cầu lông', COMMUNITY_ROOM: 'Phòng sinh hoạt', READING_ROOM: 'Phòng đọc',
  GYM: 'Phòng tập', MEETING_ROOM: 'Phòng họp',
  OWNER: 'Chủ hộ / chủ sở hữu', TENANT: 'Người thuê', FAMILY_MEMBER: 'Thành viên gia đình',
  ADMIN: 'Quản trị viên', MANAGER: 'Ban quản lý', RESIDENT: 'Cư dân', TECHNICIAN: 'Kỹ thuật viên', SECURITY: 'Bảo vệ',
  LOW: 'Thấp', NORMAL: 'Bình thường', HIGH: 'Cao', CRITICAL: 'Khẩn cấp', URGENT: 'Khẩn cấp'
}

export const severities = {
  OPEN: 'info', ASSIGNED: 'warn', IN_PROGRESS: 'warn', RESOLVED: 'success', CLOSED: 'success', CANCELLED: 'secondary',
  CONFIRMED: 'success', COMPLETED: 'info', NO_SHOW: 'danger', ACTIVE: 'success', CHECKED_IN: 'info', CHECKED_OUT: 'secondary', EXPIRED: 'danger',
  AVAILABLE: 'success', MAINTENANCE: 'warn', OUT_OF_SERVICE: 'danger', LOW: 'secondary', NORMAL: 'info', HIGH: 'warn', CRITICAL: 'danger', URGENT: 'danger'
}

export const labelFor = (value) => labels[value] || value || '—'
export const severityFor = (value) => severities[value] || 'secondary'
export const formatDate = (value) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
export const formatTime = (value) => value ? String(value).slice(0, 5) : '—'
