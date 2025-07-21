package com.islandhop.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for group messages.
 * Used for sending messages to chat groups.
 */
public class GroupMessageDTO {

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Content is required")
    private String content;

    private String messageType;
    private String senderName;

    public GroupMessageDTO() {
        this.messageType = "TEXT";
    }

    public GroupMessageDTO(String senderId, String groupId, String content) {
        this();
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
    }

    // Getters and Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
