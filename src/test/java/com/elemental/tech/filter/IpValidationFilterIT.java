package com.elemental.tech.filter;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IpValidationFilterIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideIpApiUrl(DynamicPropertyRegistry registry) {
        registry.add("ip-validation.ip-api.base-url", () -> wireMock.baseUrl() + "/json");
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void blockedCountryReturnsForbidden() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/json/1.2.3.4"))
                .willReturn(okJson("""
                        {"status":"success","country":"China","countryCode":"CN","isp":"ChinaNet","org":"ChinaNet","query":"1.2.3.4"}
                        """)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/work-items").header("X-Forwarded-For", "1.2.3.4"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void blockedIspReturnsForbidden() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/json/5.6.7.8"))
                .willReturn(okJson("""
                        {"status":"success","country":"Ireland","countryCode":"IE","isp":"Amazon.com Inc","org":"Amazon Web Services","query":"5.6.7.8"}
                        """)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/work-items").header("X-Forwarded-For", "5.6.7.8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void allowedRequestPassesThrough() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/json/9.10.11.12"))
                .willReturn(okJson("""
                        {"status":"success","country":"Germany","countryCode":"DE","isp":"Deutsche Telekom","org":"Deutsche Telekom","query":"9.10.11.12"}
                        """)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/work-items").header("X-Forwarded-For", "9.10.11.12"))
                .andExpect(status().isOk());
    }
}