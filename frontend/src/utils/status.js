export const labels = {
  OPEN: 'Mới tiếp nhận', ASSIGNED: 'Đã phân công', IN_PROGRESS: 'Đang xử lý', RESOLVED: 'Chờ xác nhận', CLOSED: 'Hoàn thành', CANCELLED: 'Đã hủy',
  CONFIRMED: 'Đã xác nhận', COMPLETED: 'Hoàn thành', NO_SHOW: 'Không đến',
  ACTIVE: 'Đang hoạt động', INACTIVE: 'Ngừng hoạt động', UNAVAILABLE: 'Không khả dụng', PENDING: 'Đang chờ', CHECKED_IN: 'Đã vào', CHECKED_OUT: 'Đã ra', EXPIRED: 'Hết hạn',
  AVAILABLE: 'Có thể sử dụng', MAINTENANCE: 'Bảo trì', OUT_OF_SERVICE: 'Ngừng hoạt động',
  ELECTRICAL: 'Điện', PLUMBING: 'Nước', ELEVATOR: 'Thang máy', CLEANING: 'Khu vực chung', OTHER: 'Khác',
  BADMINTON_COURT: 'Sân cầu lông', COMMUNITY_ROOM: 'Phòng sinh hoạt', READING_ROOM: 'Phòng đọc',
  GYM: 'Phòng tập', MEETING_ROOM: 'Phòng họp', OTHER_FACILITY: 'Tiện ích khác',
  OWNER: 'Chủ hộ', TENANT: 'Người thuê', FAMILY_MEMBER: 'Thành viên gia đình',
  ADMIN: 'Quản trị viên', MANAGER: 'Ban quản lý', RESIDENT: 'Cư dân', TECHNICIAN: 'Kỹ thuật viên', SECURITY: 'Bảo vệ',
  SERVICE_REQUEST_CREATED: 'Đã tạo báo sự cố', SERVICE_REQUEST_ASSIGNED: 'Đã phân công sự cố', SERVICE_REQUEST_STARTED: 'Đã bắt đầu xử lý', SERVICE_REQUEST_RESOLVED: 'Đã xử lý xong', SERVICE_REQUEST_CLOSED: 'Đã xác nhận hoàn thành', SERVICE_REQUEST_ASSIGNED_NOTIFICATION: 'Đã phân công báo sự cố', SERVICE_REQUEST_RESOLVED_NOTIFICATION: 'Đã cập nhật kết quả xử lý',
  BOOKING_CONFIRMED: 'Đã xác nhận lịch đặt', BOOKING_CANCELLED: 'Đã hủy lịch đặt', VISITOR_PASS_CREATED: 'Đã tạo thẻ khách', VISITOR_CHECKED_IN: 'Khách đã vào', VISITOR_CHECKED_OUT: 'Khách đã ra', VISITOR_CANCELLED: 'Đã hủy thẻ khách', FACILITY_CREATED: 'Đã thêm tiện ích', FACILITY_UPDATED: 'Đã cập nhật tiện ích',
  SERVICE_REQUEST: 'Báo sự cố', BOOKING: 'Lịch đặt', VISITOR_PASS: 'Thẻ khách', FACILITY: 'Tiện ích',
  LOW: 'Thấp', NORMAL: 'Bình thường', HIGH: 'Cao', CRITICAL: 'Khẩn cấp', URGENT: 'Khẩn cấp'
}

export const severities = {
  OPEN: 'info', ASSIGNED: 'warn', IN_PROGRESS: 'warn', RESOLVED: 'success', CLOSED: 'success', CANCELLED: 'secondary',
  CONFIRMED: 'success', COMPLETED: 'info', NO_SHOW: 'danger', ACTIVE: 'success', CHECKED_IN: 'info', CHECKED_OUT: 'secondary', EXPIRED: 'danger',
  AVAILABLE: 'success', MAINTENANCE: 'warn', OUT_OF_SERVICE: 'danger', LOW: 'secondary', NORMAL: 'info', HIGH: 'warn', CRITICAL: 'danger', URGENT: 'danger'
}

export const labelFor = (value) => labels[value] || '—'
export const severityFor = (value) => severities[value] || 'secondary'
export const auditDescription = (action, description) => action === 'BOOKING_CANCELLED' && description === 'Booking cancelled'
  ? 'Lịch đặt đã được hủy.'
  : description || '—'
export const formatDate = (value) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
export const formatDateOnly = (value) => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' }).format(new Date(value)) : '—'
export const formatTime = (value) => value ? String(value).slice(0, 5) : '—'
