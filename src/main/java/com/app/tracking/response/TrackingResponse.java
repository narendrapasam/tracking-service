package com.app.tracking.response;

import java.time.ZonedDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Tracking Response")
public class TrackingResponse {
    private String trackingNumber;
    private ZonedDateTime createdAt;
}