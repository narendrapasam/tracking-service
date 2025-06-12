package com.app.tracking.controller;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.tracking.response.TrackingResponse;
import com.app.tracking.service.TrackingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
@RestController
@AllArgsConstructor
public class TrackingController {
    private final TrackingService trackingService;
    
    @Operation(
    	    summary = "Get next tracking number",
    	    description = "Generates a unique tracking number"
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "Successfully generated tracking number",
    	                 content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
    	    @ApiResponse(responseCode = "400", description = "Invalid request parameters",
    	                 content = @Content),
    	    @ApiResponse(responseCode = "500", description = "Internal server error",
    	                 content = @Content)
    	})
    @GetMapping("/next-tracking-number")
    public ResponseEntity<TrackingResponse> getTrackingNumber(
            @RequestParam("origin_country_id") @Pattern(regexp = "^[A-Z]{2}$") String originCountryId,
            @RequestParam("destination_country_id") @Pattern(regexp = "^[A-Z]{2}$") String destinationCountryId,
            @RequestParam("weight") @DecimalMax("9999.999") double weight,
            @RequestParam("created_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime createdAt,
            @RequestParam("customer_id") UUID customerId,
            @RequestParam("customer_name") String customerName,
            @RequestParam("customer_slug") String customerSlug
    ) {
        return ResponseEntity.ok(trackingService.generateTrackingNumber(originCountryId, destinationCountryId, weight, createdAt, customerId, customerName, customerSlug));
    }
}
