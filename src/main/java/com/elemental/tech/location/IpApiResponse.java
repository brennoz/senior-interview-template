package com.elemental.tech.location;

public record IpApiResponse(
        String status,
        String message,
        String country,
        String countryCode,
        String isp,
        String org,
        String query
) {
    public boolean isSuccess() {
        return "success".equals(status);
    }
}
