package com.elemental.tech.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "request_audit_log")
public class RequestAuditLog {

    @Id
    @UuidGenerator
    @GeneratedValue
    private UUID id;

    private String requestUri;
    private Instant requestTimestamp;
    private int httpResponseCode;
    private String requestIp;
    private String countryCode;
    private String ipProvider;
    private long timeLapsedMs;

    public RequestAuditLog(String requestUri, Instant requestTimestamp,
                           String requestIp, String countryCode, String ipProvider) {
        this.requestUri = requestUri;
        this.requestTimestamp = requestTimestamp;
        this.requestIp = requestIp;
        this.countryCode = countryCode;
        this.ipProvider = ipProvider;
    }

    public void complete(int httpResponseCode, long timeLapsedMs) {
        this.httpResponseCode = httpResponseCode;
        this.timeLapsedMs = timeLapsedMs;
    }
}
