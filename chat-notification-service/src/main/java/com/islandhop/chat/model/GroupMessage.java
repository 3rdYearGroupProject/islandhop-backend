package com.islandhop.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * MongoDB entity representing a group chat message.
 * Stored in the 'group_messages' collection.
 */
@Document(collection = "group_messages")
public class GroupMessage {

    @Id
    private String id;

    @Field("sender_id")
    private String senderId;

    @Field("group_id")
    private String groupId;

    @Field("content")
    private String content;

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("message_type")
    private String messageType; // TEXT, IMAGE, FILE

    @Field("sender_name")
    private String senderName;

    public GroupMessage() {
        this.timestamp = LocalDateTime.now();
        this.messageType = "TEXT";
    }

    public GroupMessage(String senderId, String groupId, String content) {
        this();
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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
