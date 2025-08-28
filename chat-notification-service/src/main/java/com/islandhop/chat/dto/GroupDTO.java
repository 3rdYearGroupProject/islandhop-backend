package com.islandhop.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Data Transfer Object for creating chat groups.
 * Used for group creation and management operations.
 */
public class GroupDTO {

    @NotBlank(message = "Group name is required")
    private String groupName;

    @NotEmpty(message = "Member IDs list cannot be empty")
    private List<String> memberIds;

    private String adminId;
    private String description;
    private String groupType;
    private String tripId; // Associated trip ID for travel groups

    public GroupDTO() {
        this.groupType = "PRIVATE";
    }

    public GroupDTO(String groupName, List<String> memberIds) {
        this();
        this.groupName = groupName;
        this.memberIds = memberIds;
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getCreatedBy() {
        return adminId; // Return adminId as createdBy
    }
}
