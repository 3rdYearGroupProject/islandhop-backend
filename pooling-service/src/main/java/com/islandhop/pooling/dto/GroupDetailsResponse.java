package com.islandhop.pooling.dto;

import com.islandhop.pooling.model.GroupAction;
import com.islandhop.pooling.model.JoinRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for group details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDetailsResponse {
    
    private String status;
    
    private String groupId;
    
    private String groupName;
    
    private String tripId;
    
    private List<String> userIds;
    
    private String visibility;
    
    private Map<String, Object> preferences;
    
    private List<GroupAction> actions;
    
    private List<JoinRequest> joinRequests; // Only for creators
    
    private String message;
}
