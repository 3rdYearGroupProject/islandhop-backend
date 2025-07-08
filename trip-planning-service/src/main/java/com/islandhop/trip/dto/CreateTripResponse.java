package com.islandhop.trip.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Response DTO for trip creation endpoint.
 * Contains the result status, generated trip ID, and success message.
 */
@Data
@RequiredArgsConstructor
public class CreateTripResponse {

    private final String status;
    private final String tripId;
    private final String message;
}
