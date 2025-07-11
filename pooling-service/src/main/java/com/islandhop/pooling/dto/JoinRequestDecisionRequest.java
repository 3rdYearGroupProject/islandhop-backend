package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for approving or rejecting join requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDecisionRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(approved|rejected)$", message = "Status must be either 'approved' or 'rejected'")
    private String status;
}
