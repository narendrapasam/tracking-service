package com.app.tracking.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import com.app.tracking.exception.TrackingException;
import com.app.tracking.response.TrackingResponse;
import com.app.tracking.service.TrackingService;
import com.app.tracking.utility.SecureCodeGenerator;

import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private static final String ERR_MSG = "Failed to generate tracking number";
    private final SecureCodeGenerator secureCodeGenerator;
    private static final ThreadLocal<MessageDigest> SHA256_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    });
	@Override
	public TrackingResponse generateTrackingNumber(String origin, String destination, double weight,
			ZonedDateTime createdAt, UUID customerId, String customerName, String customerSlug) {
		try {
        String input = String.join("|",
                origin.toUpperCase(Locale.ROOT),
                destination.toUpperCase(Locale.ROOT),
                String.format(Locale.ROOT, "%.3f", weight),
                String.valueOf(createdAt.toInstant().toEpochMilli()),
                customerId.toString(),
                customerName,
                customerSlug,
                secureCodeGenerator.generateRandomCode(6) 
        );

        byte[] hashBytes = SHA256_DIGEST.get().digest(input.getBytes(StandardCharsets.UTF_8));
        String sha256Hex = HexFormat.of().formatHex(hashBytes).toUpperCase();
        

        if (sha256Hex == null || sha256Hex.length() < 16) {
            throw new TrackingException(ERR_MSG);
        }
        String trackingNumber = sha256Hex.substring(0, 16);

        return TrackingResponse.builder()
                .trackingNumber(trackingNumber)
                .createdAt(ZonedDateTime.now())
                .build();
		}
		catch(Exception e) {
			throw new TaskRejectedException(ERR_MSG, e);
		}
	}
	
	    }


