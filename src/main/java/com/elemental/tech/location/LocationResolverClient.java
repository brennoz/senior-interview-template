package com.elemental.tech.location;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class LocationResolverClient {

    private static final IpApiResponse PRIVATE_IP_RESPONSE =
            new IpApiResponse("success", null, "PRIVATE", "PRIVATE", "PRIVATE", "PRIVATE", null);

    private final RestClient restClient;
    private final String fields;

    public LocationResolverClient(
            @Value("${ip-validation.ip-api.base-url}") String baseUrl,
            @Value("${ip-validation.ip-api.fields}") String fields,
            @Value("${ip-validation.ip-api.connect-timeout-ms}") int connectTimeoutMs,
            @Value("${ip-validation.ip-api.read-timeout-ms}") int readTimeoutMs) {

        this.fields = fields;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 1.5)
    )
    public IpApiResponse resolve(String ip) {
        if (isPrivateIp(ip)) {
            log.debug("Private IP {}, skipping location lookup", ip);
            return PRIVATE_IP_RESPONSE;
        }

        log.info("Resolving location for IP: {}", ip);
        IpApiResponse response = restClient.get()
                .uri("/{ip}?fields={fields}", ip, fields)
                .retrieve()
                .body(IpApiResponse.class);

        log.info("Location resolved — ip={}, country={}, isp={}", ip, response.countryCode(), response.isp());
        return response;
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1");
    }
}
