package com.islandhop.pooling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for joining an existing group.
 */
@Data
@AllArgsConstructor
public class JoinExistingGroupResponse {
    private String groupId;
    private String message;
}
