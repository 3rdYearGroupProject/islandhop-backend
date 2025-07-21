package com.islandhop.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB entity representing a chat group.
 * Stored in the 'groups' collection.
 */
@Document(collection = "groups")
public class Group {

    @Id
    private String id;

    @Field("group_name")
    private String groupName;

    @Field("member_ids")
    private List<String> memberIds;

    @Field("admin_id")
    private String adminId;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("description")
    private String description;

    @Field("group_type")
    private String groupType; // PUBLIC, PRIVATE

    public Group() {
        this.createdAt = LocalDateTime.now();
        this.groupType = "PRIVATE";
    }

    public Group(String groupName, List<String> memberIds, String adminId) {
        this();
        this.groupName = groupName;
        this.memberIds = memberIds;
        this.adminId = adminId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}
