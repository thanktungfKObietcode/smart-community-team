package vn.edu.crs.smartcommunity.audit.internal.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.smartcommunity.audit.api.AuditInfo;
import vn.edu.crs.smartcommunity.audit.api.AuditLookup;
import vn.edu.crs.smartcommunity.audit.internal.entity.AuditLog;
import vn.edu.crs.smartcommunity.audit.internal.repository.AuditLogRepository;
import vn.edu.crs.smartcommunity.identity.api.IdentityLookup;

@Service
public class AuditLogService implements AuditLookup {

    private final AuditLogRepository repository;
    private final IdentityLookup identityLookup;

    public AuditLogService(AuditLogRepository repository, IdentityLookup identityLookup) {
        this.repository = repository;
        this.identityLookup = identityLookup;
    }

    @Transactional
    public void record(Long actorUserId, String action, String entityType, Long entityId, String description) {
        repository.save(new AuditLog(actorUserId, action, entityType, entityId, description));
    }

    @Transactional(readOnly = true)
    public List<AuditInfo> search(String action, String entityType, Long actorUserId) {
        return repository.findAll().stream()
                .filter(log -> action == null || action.equalsIgnoreCase(log.getAction()))
                .filter(log -> entityType == null || entityType.equalsIgnoreCase(log.getEntityType()))
                .filter(log -> actorUserId == null || actorUserId.equals(log.getActorUserId()))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .map(this::toInfo).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditInfo> latest(int limit) {
        return search(null, null, null).stream().limit(Math.max(0, limit)).toList();
    }

    private AuditInfo toInfo(AuditLog log) {
        String actorName = log.getActorUserId() == null ? null
                : identityLookup.getUserById(log.getActorUserId()).map(info -> info.fullName()).orElse(null);
        return new AuditInfo(log.getAction(), log.getEntityType(), log.getEntityId(), log.getDescription(),
                log.getCreatedAt(), log.getActorUserId(), actorName);
    }
}
