package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for compatible groups.
 */
@Data
@AllArgsConstructor
public class CompatibleGroupResponse {
    private String groupId;
    private double compatibilityScore;
}
