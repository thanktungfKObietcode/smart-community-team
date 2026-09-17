package vn.edu.crs.smartcommunity.audit.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.crs.smartcommunity.audit.internal.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
