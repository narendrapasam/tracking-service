package com.app.tracking.service.impl;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.app.tracking.exception.TrackingException;
import com.app.tracking.response.TrackingResponse;
import com.app.tracking.service.TrackingService;
import com.app.tracking.utility.SecureCodeGenerator;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private static final long CUSTOM_EPOCH = 1_650_000_000_000L;
    private static final String ERR_MSG = "Failed to generate tracking number";

    private static final ThreadLocal<MessageDigest> SHA256_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    });

    private final SecureCodeGenerator secureCodeGenerator;

    @Override
    public TrackingResponse generateTrackingNumber(String origin, String destination, double weight,
                                                   ZonedDateTime createdAt, UUID customerId,
                                                   String customerName, String customerSlug) {
        try {
            String trackingNumber = generateSafeTrackingNumber(
                    origin, destination, weight, createdAt,
                    customerId, customerName, customerSlug);

            return TrackingResponse.builder()
                    .createdAt(ZonedDateTime.now())
                    .trackingNumber(trackingNumber)
                    .build();
        } catch (Exception e) {
            throw new TrackingException(ERR_MSG, e);
        }
    }

    private String generateSafeTrackingNumber(String origin, String destination, double weight,
                                                            ZonedDateTime createdAt, UUID customerId,
                                                            String customerName, String customerSlug) {
        String businessInput = String.join("|",
                origin.toUpperCase(Locale.ROOT),
                destination.toUpperCase(Locale.ROOT),
                String.format(Locale.ROOT, "%.3f", weight),
                String.valueOf(createdAt.toInstant().toEpochMilli()),
                customerId.toString(),
                customerName,
                customerSlug,
                secureCodeGenerator.generateRandomCode(6)
        );


        byte[] hash = SHA256_DIGEST.get().digest(businessInput.getBytes(StandardCharsets.UTF_8));
        String prefix = HexFormat.of().formatHex(hash).toUpperCase().substring(0, 3);


        String ipChar;
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            int lastByte = ip[ip.length - 1] & 0xFF;
            ipChar = Integer.toString(lastByte % 36, 36).toUpperCase();
        } catch (Exception e) {
            ipChar = Integer.toString(ThreadLocalRandom.current().nextInt(36), 36).toUpperCase();
        }

        long timestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        if (timestamp >= (1L << 42)) {
            throw new IllegalStateException("Timestamp out of range");
        }

        byte[] randomBytes = new byte[10]; 
        ThreadLocalRandom.current().nextBytes(randomBytes);
        BigInteger randomPart = new BigInteger(1, randomBytes).and(BigInteger.valueOf(1L << 74).subtract(BigInteger.ONE));

        BigInteger combined = BigInteger.valueOf(timestamp).shiftLeft(74).or(randomPart);
        String base36 = combined.toString(36).toUpperCase(Locale.ROOT);

        int suffixLength = 16 - prefix.length() - ipChar.length();
        if (base36.length() < suffixLength) {
            base36 = "0".repeat(suffixLength - base36.length()) + base36;
        } else if (base36.length() > suffixLength) {
            base36 = base36.substring(base36.length() - suffixLength);
        }

        return prefix + ipChar + base36;
    }
}
