package com.securemarts.domain.audit.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.audit.dto.AuditLogResponse;
import com.securemarts.domain.audit.entity.AuditLog;
import com.securemarts.domain.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(AuditLog.ActorType actorType, String actorPublicId, String actorLabel,
                       String action, String module, String details, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setActorType(actorType);
        log.setActorPublicId(actorPublicId);
        log.setActorLabel(actorLabel);
        log.setAction(action);
        log.setModule(module);
        log.setDetails(details);
        if (request != null) {
            log.setIpAddress(extractIp(request));
            String ua = request.getHeader("User-Agent");
            if (ua != null && ua.length() > 500) {
                ua = ua.substring(0, 500);
            }
            log.setUserAgent(ua);
        }
        auditLogRepository.save(log);
    }

    private static String extractIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> list(String search, String actorPublicId, String action,
                                              String module, Instant fromTs, Instant toTs, Pageable pageable) {
        Specification<AuditLog> spec = buildSpec(search, actorPublicId, action, module, fromTs, toTs);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(AuditLogResponse::from));
    }

    @Transactional(readOnly = true)
    public Stream<AuditLog> streamForExport(String search, String actorPublicId, String action,
                                            String module, Instant fromTs, Instant toTs) {
        Specification<AuditLog> spec = buildSpec(search, actorPublicId, action, module, fromTs, toTs);
        return auditLogRepository.findAll(spec).stream();
    }

    private Specification<AuditLog> buildSpec(String search, String actorPublicId, String action,
                                              String module, Instant fromTs, Instant toTs) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("publicId")), pattern),
                        cb.like(cb.lower(root.get("action")), pattern),
                        cb.like(cb.lower(root.get("details")), pattern)
                ));
            }
            if (actorPublicId != null && !actorPublicId.isBlank()) {
                predicates.add(cb.equal(root.get("actorPublicId"), actorPublicId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (module != null && !module.isBlank()) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (fromTs != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromTs));
            }
            if (toTs != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toTs));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
