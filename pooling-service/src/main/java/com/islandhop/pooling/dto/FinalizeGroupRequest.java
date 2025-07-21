package com.islandhop.pooling.dto;

import lombok.Data;

/**
 * Request DTO for finalizing a group.
 */
@Data
public class FinalizeGroupRequest {
    private String groupId;
    private String userId;
}
