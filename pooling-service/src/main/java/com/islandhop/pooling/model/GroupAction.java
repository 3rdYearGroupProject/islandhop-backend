package com.islandhop.pooling.model;

import lombok.Data;

import java.time.Instant;

/**
 * Represents an action performed on a group (audit log).
 * Nested within Group entity.
 * Follows consistent patterns with other model classes.
 */
@Data
public class GroupAction {
    
    private String userId;
    
    private String actionType;
    
    private String details;
    
    private Instant timestamp;
    
    /**
     * Create a new group action with current timestamp.
     */
    public static GroupAction create(String userId, String actionType, String details) {
        GroupAction action = new GroupAction();
        action.setUserId(userId);
        action.setActionType(actionType);
        action.setDetails(details);
        action.setTimestamp(Instant.now());
        return action;
    }
    
    /**
     * Common action types.
     */
    public static class ActionType {
        public static final String CREATE_GROUP = "CREATE_GROUP";
        public static final String INVITE_USER = "INVITE_USER";
        public static final String JOIN_GROUP = "JOIN_GROUP";
        public static final String LEAVE_GROUP = "LEAVE_GROUP";
        public static final String ADD_PLACE = "ADD_PLACE";
        public static final String REMOVE_PLACE = "REMOVE_PLACE";
        public static final String UPDATE_CITY = "UPDATE_CITY";
        public static final String UPDATE_PREFERENCES = "UPDATE_PREFERENCES";
        public static final String APPROVE_JOIN_REQUEST = "APPROVE_JOIN_REQUEST";
        public static final String REJECT_JOIN_REQUEST = "REJECT_JOIN_REQUEST";
    }
}
