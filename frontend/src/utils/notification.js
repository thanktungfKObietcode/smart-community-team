const assignedMessage = /Service request #(.+?) has been assigned to you:\s*(.+)$/i
const resolvedMessage = /Your service request #(.+?) has been resolved:\s*(.+)$/i

export const notificationTitle = (notification) => {
  if (notification?.type === 'SERVICE_REQUEST_ASSIGNED') return 'Yêu cầu hỗ trợ đã được phân công'
  if (notification?.type === 'SERVICE_REQUEST_RESOLVED') return 'Yêu cầu hỗ trợ đã được xử lý'
  return notification?.title || 'Thông báo'
}

export const notificationMessage = (notification) => {
  const raw = notification?.message || ''
  let match = raw.match(assignedMessage)
  if (match) return `Yêu cầu hỗ trợ #${match[1]} đã được phân công cho bạn: ${match[2]}`
  match = raw.match(resolvedMessage)
  if (match) return `Yêu cầu hỗ trợ #${match[1]} đã được xử lý: ${match[2]}`
  if (raw === 'Booking cancelled') return 'Lịch đặt đã được hủy.'
  return raw || 'Bạn có một cập nhật mới.'
}
