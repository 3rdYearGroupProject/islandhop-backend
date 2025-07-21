package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for creating a public pooling group.
 */
@Data
@AllArgsConstructor
public class CreatePublicPoolingGroupResponse {
    private String groupId;
    private String message;
}
