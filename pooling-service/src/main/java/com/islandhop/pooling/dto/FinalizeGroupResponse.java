package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for finalizing a group.
 */
@Data
@AllArgsConstructor
public class FinalizeGroupResponse {
    private String groupId;
    private String message;
}
