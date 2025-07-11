package com.islandhop.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for managing group members.
 * Used for adding and removing users from chat groups.
 */
public class GroupMemberDTO {

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String actionType; // ADD, REMOVE
    private String requesterId; // ID of user performing the action

    public GroupMemberDTO() {
        this.actionType = "ADD";
    }

    public GroupMemberDTO(String groupId, String userId) {
        this();
        this.groupId = groupId;
        this.userId = userId;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }
}
