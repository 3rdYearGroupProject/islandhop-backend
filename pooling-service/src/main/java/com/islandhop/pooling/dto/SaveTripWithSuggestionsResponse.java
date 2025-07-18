package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for saving a trip with suggestions.
 */
@Data
@AllArgsConstructor
public class SaveTripWithSuggestionsResponse {
    private String tripId;
    private String message;
}
