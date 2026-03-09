package com.securemarts.domain.admin.controller;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.domain.audit.dto.AuditLogResponse;
import com.securemarts.domain.audit.entity.AuditLog;
import com.securemarts.domain.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.stream.Stream;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Logs", description = "List and export audit logs")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit logs", description = "Paginated list with optional search and filters")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:audit:read')")
    public ResponseEntity<PageResponse<AuditLogResponse>> list(
            @Parameter(description = "Search in activity ID, action, or details") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by actor public ID") @RequestParam(required = false) String user,
            @Parameter(description = "Filter by action") @RequestParam(required = false) String action,
            @Parameter(description = "Filter by module") @RequestParam(required = false) String module,
            @Parameter(description = "From timestamp (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "To timestamp (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.list(search, user, action, module, from, to, pageable));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export audit logs as CSV", description = "Same filters as list; returns CSV file")
    @PreAuthorize("hasRole('SUPERUSER') or hasAuthority('SCOPE_admin:audit:read')")
    public ResponseEntity<String> export(
            @Parameter(description = "Search in activity ID, action, or details") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by actor public ID") @RequestParam(required = false) String user,
            @Parameter(description = "Filter by action") @RequestParam(required = false) String action,
            @Parameter(description = "Filter by module") @RequestParam(required = false) String module,
            @Parameter(description = "From timestamp (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "To timestamp (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) throws IOException {
        Stream<AuditLog> stream = auditLogService.streamForExport(search, user, action, module, from, to);
        String csv = toCsv(stream);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "audit-logs.csv");
        return ResponseEntity.ok().headers(headers).body(csv);
    }

    private static String toCsv(Stream<AuditLog> stream) throws IOException {
        StringWriter w = new StringWriter();
        w.write("Activity ID,User,Action,Module,Timestamp,IP Address\n");
        stream.map(AuditLogResponse::from).forEach(r -> {
            w.write(escapeCsv(r.getActivityId()) + ",");
            w.write(escapeCsv(r.getUser()) + ",");
            w.write(escapeCsv(r.getAction()) + ",");
            w.write(escapeCsv(r.getModule()) + ",");
            w.write(r.getTimestamp() != null ? escapeCsv(r.getTimestamp().toString()) : ",");
            w.write(escapeCsv(r.getIpAddress()) + "\n");
        });
        return w.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
