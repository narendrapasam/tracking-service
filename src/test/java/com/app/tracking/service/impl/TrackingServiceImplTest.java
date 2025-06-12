package com.app.tracking.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;

import com.app.tracking.response.TrackingResponse;
import com.app.tracking.utility.SecureCodeGenerator;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceImplTest {
	
	@InjectMocks 
    TrackingServiceImpl service;
    @Mock
    SecureCodeGenerator codeGenerator;
	 
    @Test
    void generateTrackingNumber_success() {
       
        String origin = "US";
        String destination = "IN";
        double weight = 2.345;
        ZonedDateTime createdAt = ZonedDateTime.now();
        UUID customerId = UUID.randomUUID();
        String customerName = "Test Customer";
        String customerSlug = "test-customer";

        when(codeGenerator.generateRandomCode(anyInt())).thenReturn("ABC123");
        TrackingResponse response = service.generateTrackingNumber(
                origin, destination, weight, createdAt, customerId, customerName, customerSlug
        );

       
        assertThat(response).isNotNull();
        assertThat(response.getTrackingNumber()).isNotBlank();
        assertThat(response.getTrackingNumber()).matches("^[A-Z0-9]{16}$");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    
    @Test
    void testGenerateTrackingNumber_whenRandomFails_shouldThrowTaskRejectedException() {
        when(codeGenerator.generateRandomCode(anyInt())).thenThrow(new RuntimeException("Failure"));

        assertThrows(TaskRejectedException.class, () ->
            service.generateTrackingNumber("IN", "US", 2.5, ZonedDateTime.now(),
                    UUID.randomUUID(), "Cust", "slug"));
}
    
    @Test
    void testGeneratedTrackingNumberFormat() {
        TrackingResponse response = service.generateTrackingNumber(
                "ca", "uk", 0.456, ZonedDateTime.now(), UUID.randomUUID(), "SampleName", "sample-name"
        );

        String trackingNumber = response.getTrackingNumber();
        assertTrue(trackingNumber.matches("^[A-Z0-9]{16}$"));
    }
}
