package com.elemental.tech.filter;

import com.elemental.tech.audit.RequestAuditLog;
import com.elemental.tech.audit.RequestAuditLogRepository;
import com.elemental.tech.location.IpApiResponse;
import com.elemental.tech.location.LocationResolverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class IpValidationFilter extends OncePerRequestFilter {

    private final LocationResolverClient locationResolverClient;
    private final RequestAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${ip-validation.ip-header}")
    private String ipHeader;

    @Value("${ip-validation.fail-open}")
    private boolean failOpen;

    @Value("${ip-validation.blocked-countries}")
    private List<String> blockedCountries;

    @Value("${ip-validation.blocked-isp-keywords}")
    private List<String> blockedIspKeywords;

    public IpValidationFilter(LocationResolverClient locationResolverClient,
                              RequestAuditLogRepository auditLogRepository,
                              ObjectMapper objectMapper) {
        this.locationResolverClient = locationResolverClient;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = extractIp(request);
        Instant start = Instant.now();
        int responseCode;
        String countryCode = null;
        String ipProvider = null;

        IpApiResponse location = resolveLocation(ip);

        if (location == null) {
            if (failOpen) {
                log.warn("Location lookup failed for IP {}, fail-open — allowing request", ip);
                filterChain.doFilter(request, response);
                responseCode = response.getStatus();
            } else {
                log.warn("Location lookup failed for IP {}, fail-closed — returning 503", ip);
                writeErrorResponse(response, HttpStatus.SERVICE_UNAVAILABLE, "IP validation service unavailable");
                responseCode = HttpStatus.SERVICE_UNAVAILABLE.value();
            }
        } else {
            countryCode = location.countryCode();
            ipProvider = location.isp();

            String blockReason = getBlockReason(location);
            if (blockReason != null) {
                log.warn("Blocked request from IP {} — {}", ip, blockReason);
                writeErrorResponse(response, HttpStatus.FORBIDDEN, blockReason);
                responseCode = HttpStatus.FORBIDDEN.value();
            } else {
                filterChain.doFilter(request, response);
                responseCode = response.getStatus();
            }
        }

        long elapsed = Instant.now().toEpochMilli() - start.toEpochMilli();
        saveAuditLog(request.getRequestURI(), start, ip, countryCode, ipProvider, responseCode, elapsed);
    }

    private IpApiResponse resolveLocation(String ip) {
        try {
            return locationResolverClient.resolve(ip);
        } catch (Exception ex) {
            log.error("Location resolution failed for IP {}: {}", ip, ex.getMessage());
            return null;
        }
    }

    private String getBlockReason(IpApiResponse location) {
        if (blockedCountries.contains(location.countryCode())) {
            return "Requests from " + location.country() + " are not allowed";
        }
        for (String keyword : blockedIspKeywords) {
            if (containsIgnoreCase(location.isp(), keyword) || containsIgnoreCase(location.org(), keyword)) {
                return "Requests from provider " + location.isp() + " are not allowed";
            }
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private void saveAuditLog(String uri, Instant timestamp, String ip,
                              String countryCode, String ipProvider, int responseCode, long elapsed) {
        try {
            RequestAuditLog auditLog = new RequestAuditLog(uri, timestamp, ip, countryCode, ipProvider);
            auditLog.complete(responseCode, elapsed);
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Failed to save audit log for IP {}: {}", ip, ex.getMessage());
        }
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader(ipHeader);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/h2-console");
    }
}
