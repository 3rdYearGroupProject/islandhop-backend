package com.islandhop.chat.service;

import com.islandhop.chat.dto.GroupDTO;
import com.islandhop.chat.dto.GroupMemberDTO;
import com.islandhop.chat.dto.GroupMessageDTO;
import com.islandhop.chat.model.Group;
import com.islandhop.chat.model.GroupMessage;
import com.islandhop.chat.repository.mongo.GroupRepository;
import com.islandhop.chat.repository.mongo.GroupMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling group chat operations.
 * Manages group creation, member management, message persistence, and real-time delivery.
 */
@Service
public class GroupChatService {

    private static final Logger logger = LoggerFactory.getLogger(GroupChatService.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${chat.redis.group-channel-prefix:chat:group:}")
    private String groupChannelPrefix;

    /**
     * Create a new chat group.
     * 
     * @param groupDTO The group data transfer object
     * @return The created Group entity
     */
    public Group createGroup(GroupDTO groupDTO) {
        logger.info("Creating new group: {}", groupDTO.getGroupName());

        Group group = new Group(
                groupDTO.getGroupName(),
                groupDTO.getMemberIds(),
                groupDTO.getAdminId()
        );
        group.setDescription(groupDTO.getDescription());
        group.setGroupType(groupDTO.getGroupType());

        Group savedGroup = groupRepository.save(group);
        logger.debug("Group created with ID: {}", savedGroup.getId());

        // Notify all members about group creation
        notifyGroupMembers(savedGroup, "GROUP_CREATED", "You have been added to group: " + savedGroup.getGroupName());

        return savedGroup;
    }

    /**
     * Send a message to a group.
     * 
     * @param groupMessageDTO The group message data transfer object
     * @return The saved GroupMessage entity
     */
    public GroupMessage sendGroupMessage(GroupMessageDTO groupMessageDTO) {
        logger.info("Sending group message to group {} from user {}", 
                   groupMessageDTO.getGroupId(), groupMessageDTO.getSenderId());

        // Verify group exists and user is a member
        Optional<Group> groupOpt = groupRepository.findById(groupMessageDTO.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        Group group = groupOpt.get();
        if (!group.getMemberIds().contains(groupMessageDTO.getSenderId())) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        // Create and save message
        GroupMessage message = new GroupMessage(
                groupMessageDTO.getSenderId(),
                groupMessageDTO.getGroupId(),
                groupMessageDTO.getContent()
        );
        message.setMessageType(groupMessageDTO.getMessageType());
        message.setSenderName(groupMessageDTO.getSenderName());

        GroupMessage savedMessage = groupMessageRepository.save(message);
        logger.debug("Group message saved with ID: {}", savedMessage.getId());

        // Broadcast via Redis pub/sub
        broadcastGroupMessageViaRedis(savedMessage);

        // Send via WebSocket
        sendGroupMessageViaWebSocket(savedMessage);

        return savedMessage;
    }

    /**
     * Get all messages from a group.
     * 
     * @param groupId The group's ID
     * @return List of messages in the group
     */
    public List<GroupMessage> getGroupMessages(String groupId) {
        logger.info("Retrieving messages for group: {}", groupId);
        
        List<GroupMessage> messages = groupMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);
        logger.debug("Found {} messages in group", messages.size());
        
        return messages;
    }

    /**
     * Add a member to a group.
     * 
     * @param groupMemberDTO The group member data transfer object
     * @return The updated Group entity
     */
    public Group addMemberToGroup(GroupMemberDTO groupMemberDTO) {
        logger.info("Adding user {} to group {}", groupMemberDTO.getUserId(), groupMemberDTO.getGroupId());

        Optional<Group> groupOpt = groupRepository.findById(groupMemberDTO.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        Group group = groupOpt.get();
        
        // Check if user is already a member
        if (group.getMemberIds().contains(groupMemberDTO.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        // Add user to group
        group.getMemberIds().add(groupMemberDTO.getUserId());
        Group updatedGroup = groupRepository.save(group);

        logger.debug("User {} added to group {}", groupMemberDTO.getUserId(), groupMemberDTO.getGroupId());

        // Notify group members
        notifyGroupMembers(updatedGroup, "MEMBER_ADDED", 
                          "User " + groupMemberDTO.getUserId() + " joined the group");

        return updatedGroup;
    }

    /**
     * Remove a member from a group.
     * 
     * @param groupMemberDTO The group member data transfer object
     * @return The updated Group entity
     */
    public Group removeMemberFromGroup(GroupMemberDTO groupMemberDTO) {
        logger.info("Removing user {} from group {}", groupMemberDTO.getUserId(), groupMemberDTO.getGroupId());

        Optional<Group> groupOpt = groupRepository.findById(groupMemberDTO.getGroupId());
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found");
        }

        Group group = groupOpt.get();
        
        // Check if user is a member
        if (!group.getMemberIds().contains(groupMemberDTO.getUserId())) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        // Remove user from group
        group.getMemberIds().remove(groupMemberDTO.getUserId());
        Group updatedGroup = groupRepository.save(group);

        logger.debug("User {} removed from group {}", groupMemberDTO.getUserId(), groupMemberDTO.getGroupId());

        // Notify group members
        notifyGroupMembers(updatedGroup, "MEMBER_REMOVED", 
                          "User " + groupMemberDTO.getUserId() + " left the group");

        return updatedGroup;
    }

    /**
     * Get all groups for a user.
     * 
     * @param userId The user's ID
     * @return List of groups the user belongs to
     */
    public List<Group> getUserGroups(String userId) {
        logger.info("Retrieving groups for user: {}", userId);
        
        List<Group> groups = groupRepository.findGroupsByMemberId(userId);
        logger.debug("Found {} groups for user", groups.size());
        
        return groups;
    }

    /**
     * Broadcast group message via Redis pub/sub.
     * 
     * @param message The message to broadcast
     */
    private void broadcastGroupMessageViaRedis(GroupMessage message) {
        try {
            String groupChannel = groupChannelPrefix + message.getGroupId();
            redisTemplate.convertAndSend(groupChannel, message);
            logger.debug("Group message broadcasted via Redis to channel: {}", groupChannel);
        } catch (Exception e) {
            logger.error("Failed to broadcast group message via Redis: {}", e.getMessage());
        }
    }

    /**
     * Send group message via WebSocket.
     * 
     * @param message The message to send
     */
    private void sendGroupMessageViaWebSocket(GroupMessage message) {
        try {
            // Send to group topic
            messagingTemplate.convertAndSend(
                    "/topic/group/" + message.getGroupId(),
                    message
            );
            logger.debug("Group message sent via WebSocket to group: {}", message.getGroupId());
        } catch (Exception e) {
            logger.error("Failed to send group message via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Notify all group members about an event.
     * 
     * @param group The group
     * @param eventType The type of event
     * @param message The notification message
     */
    private void notifyGroupMembers(Group group, String eventType, String message) {
        try {
            for (String memberId : group.getMemberIds()) {
                messagingTemplate.convertAndSendToUser(
                        memberId,
                        "/queue/notifications",
                        Map.of(
                                "type", eventType,
                                "groupId", group.getId(),
                                "groupName", group.getGroupName(),
                                "message", message
                        )
                );
            }
            logger.debug("Notified {} group members about {}", group.getMemberIds().size(), eventType);
        } catch (Exception e) {
            logger.error("Failed to notify group members: {}", e.getMessage());
        }
    }
}
