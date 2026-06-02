package com.virtulab.platform.audit.web;

import com.virtulab.platform.audit.store.AuditRepository;
import com.virtulab.platform.contracts.audit.AuditEventPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditRepository repository;

    public AuditController(AuditRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/events")
    public AuditEventPage events(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return repository.findRecent(userId, Math.min(limit, 200));
    }
}
