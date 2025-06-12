package com.app.tracking.service;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.app.tracking.response.TrackingResponse;

public interface TrackingService {
	TrackingResponse generateTrackingNumber(String origin, String destination, double weight, ZonedDateTime createdAt,
			UUID customerId, String customerName, String customerSlug);
}
