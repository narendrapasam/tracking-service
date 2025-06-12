package com.app.tracking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.app.tracking.exception.GlobalExceptionHandler;
import com.app.tracking.exception.TrackingException;
import com.app.tracking.response.TrackingResponse;
import com.app.tracking.service.TrackingService;

@WebMvcTest(TrackingController.class)
@Import(GlobalExceptionHandler.class)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrackingService trackingService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public TrackingService trackingService() {
            return Mockito.mock(TrackingService.class);
        }
    }

    @Test
    void shouldReturnTrackingNumber() throws Exception {
        UUID customerId = UUID.randomUUID();
        ZonedDateTime createdAt = ZonedDateTime.now();
        TrackingResponse mockResponse = TrackingResponse.builder()
            .trackingNumber("XYZ123456789")
            .createdAt(createdAt)
            .build();

        when(trackingService.generateTrackingNumber(
                eq("IN"), eq("US"), eq(1.5), any(), eq(customerId), eq("John"), eq("slug")))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/next-tracking-number")
                .param("origin_country_id", "IN")
                .param("destination_country_id", "US")
                .param("weight", "1.5")
                .param("created_at", createdAt.toString())
                .param("customer_id", customerId.toString())
                .param("customer_name", "John")
                .param("customer_slug", "slug"))

            .andExpect(jsonPath("$.trackingNumber").value("XYZ123456789"));
    }
    
    @Test
    void shouldReturnException() throws Exception {
    	when(trackingService.generateTrackingNumber(
                "IN", "US", 1.5, ZonedDateTime.parse("2023-06-11T10:00:00Z"),
                UUID.fromString("00000000-0000-0000-0000-000000000001"), "John", "slug"))
                .thenThrow(new TrackingException("Something went wrong"));

        mockMvc.perform(get("/next-tracking-number")
                        .param("origin_country_id", "IN")
                        .param("destination_country_id", "US")
                        .param("weight", "1.5")
                        .param("created_at", "2023-06-11T10:00:00Z")
                        .param("customer_id", "00000000-0000-0000-0000-000000000001")
                        .param("customer_name", "John")
                        .param("customer_slug", "slug")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Something went wrong"));
    }
}
