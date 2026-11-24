package com.elemental.tech.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestAuditLogRepository extends JpaRepository<RequestAuditLog, UUID> {
}
