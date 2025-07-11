package com.islandhop.chat.controller;

import com.islandhop.chat.dto.GroupDTO;
import com.islandhop.chat.dto.GroupMemberDTO;
import com.islandhop.chat.dto.GroupMessageDTO;
import com.islandhop.chat.model.Group;
import com.islandhop.chat.model.GroupMessage;
import com.islandhop.chat.service.GroupChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for handling group chat operations.
 * Provides endpoints for group management and group messaging.
 */
@RestController
@RequestMapping("/api/v1/chat/group")
@CrossOrigin(origins = "*")
public class GroupChatController {

    private static final Logger logger = LoggerFactory.getLogger(GroupChatController.class);

    @Autowired
    private GroupChatService groupChatService;

    /**
     * Create a new group.
     * 
     * @param groupDTO The group data transfer object
     * @return ResponseEntity with the created group
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupDTO groupDTO) {
        logger.info("Creating new group: {} by creator: {}", groupDTO.getGroupName(), groupDTO.getCreatedBy());

        try {
            Group createdGroup = groupChatService.createGroup(groupDTO);
            logger.debug("Group created successfully with ID: {}", createdGroup.getId());
            
            return ResponseEntity.ok(createdGroup);
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to create group: " + e.getMessage());
        }
    }

    /**
     * Send a message to a group.
     * 
     * @param groupMessageDTO The group message data transfer object
     * @return ResponseEntity with the saved message
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendGroupMessage(@Valid @RequestBody GroupMessageDTO groupMessageDTO) {
        logger.info("Sending group message to group {} from user {}", 
                   groupMessageDTO.getGroupId(), groupMessageDTO.getSenderId());

        try {
            GroupMessage savedMessage = groupChatService.sendGroupMessage(groupMessageDTO);
            logger.debug("Group message sent successfully with ID: {}", savedMessage.getId());
            
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            logger.error("Error sending group message: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Add a member to a group.
     * 
     * @param groupMemberDTO The group member data transfer object
     * @return ResponseEntity with the updated group
     */
    @PostMapping("/add-member")
    public ResponseEntity<?> addMemberToGroup(@Valid @RequestBody GroupMemberDTO groupMemberDTO) {
        logger.info("Adding member {} to group {}", groupMemberDTO.getUserId(), groupMemberDTO.getGroupId());

        try {
            Group updatedGroup = groupChatService.addMemberToGroup(groupMemberDTO);
            logger.debug("Member added to group successfully");
            
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            logger.error("Error adding member to group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add member: " + e.getMessage());
        }
    }

    /**
     * Remove a member from a group.
     * 
     * @param groupId The group ID
     * @param userId The user ID to remove
     * @param removedBy The user ID who is removing the member
     * @return ResponseEntity with the updated group
     */
    @DeleteMapping("/{groupId}/member/{userId}")
    public ResponseEntity<?> removeMemberFromGroup(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestParam String removedBy) {
        
        logger.info("Removing member {} from group {} by user {}", userId, groupId, removedBy);

        try {
            // Create GroupMemberDTO for the service call
            GroupMemberDTO groupMemberDTO = new GroupMemberDTO(groupId, userId);
            groupMemberDTO.setRequesterId(removedBy);
            
            Group updatedGroup = groupChatService.removeMemberFromGroup(groupMemberDTO);
            logger.debug("Member removed from group successfully");
            
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            logger.error("Error removing member from group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to remove member: " + e.getMessage());
        }
    }

    /**
     * Get group messages with pagination.
     * 
     * @param groupId The group ID
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return ResponseEntity with paginated group messages
     */
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<?> getGroupMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Retrieving messages for group {} (page: {}, size: {})", groupId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GroupMessage> messages = groupChatService.getGroupMessages(groupId, pageable);
            
            logger.debug("Retrieved {} messages for group", messages.getTotalElements());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error retrieving group messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve messages: " + e.getMessage());
        }
    }

    /**
     * Get groups for a user.
     * 
     * @param userId The user ID
     * @return ResponseEntity with list of groups the user belongs to
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable String userId) {
        logger.info("Retrieving groups for user: {}", userId);

        try {
            List<Group> groups = groupChatService.getUserGroups(userId);
            logger.debug("Found {} groups for user", groups.size());
            
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error retrieving user groups: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve groups: " + e.getMessage());
        }
    }

    /**
     * Get group details by ID.
     * 
     * @param groupId The group ID
     * @return ResponseEntity with group details
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupById(@PathVariable String groupId) {
        logger.info("Retrieving group details for ID: {}", groupId);

        try {
            Optional<Group> groupOpt = groupChatService.getGroupById(groupId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Group group = groupOpt.get();
            logger.debug("Retrieved group details successfully");
            
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            logger.error("Error retrieving group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve group: " + e.getMessage());
        }
    }

    /**
     * Update group information.
     * 
     * @param groupId The group ID
     * @param groupDTO The updated group data
     * @return ResponseEntity with the updated group
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable String groupId,
            @Valid @RequestBody GroupDTO groupDTO) {
        
        logger.info("Updating group {} with new information", groupId);

        try {
            Group updatedGroup = groupChatService.updateGroup(groupId, groupDTO);
            logger.debug("Group updated successfully");
            
            return ResponseEntity.ok(updatedGroup);
        } catch (Exception e) {
            logger.error("Error updating group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update group: " + e.getMessage());
        }
    }

    /**
     * Delete a group.
     * 
     * @param groupId The group ID
     * @param deletedBy The user ID who is deleting the group
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable String groupId,
            @RequestParam String deletedBy) {
        
        logger.info("Deleting group {} by user {}", groupId, deletedBy);

        try {
            groupChatService.deleteGroup(groupId, deletedBy);
            logger.debug("Group deleted successfully");
            
            return ResponseEntity.ok("Group deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to delete group: " + e.getMessage());
        }
    }

    /**
     * Search messages in a group.
     * 
     * @param groupId The group ID
     * @param searchTerm The search term
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return ResponseEntity with search results
     */
    @GetMapping("/{groupId}/search")
    public ResponseEntity<?> searchGroupMessages(
            @PathVariable String groupId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Searching messages in group {} for term: {}", groupId, searchTerm);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GroupMessage> searchResults = groupChatService.searchGroupMessages(
                    groupId, searchTerm, pageable);
            
            logger.debug("Found {} messages matching search term", searchResults.getTotalElements());
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            logger.error("Error searching group messages: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to search messages: " + e.getMessage());
        }
    }

    /**
     * Get group member list.
     * 
     * @param groupId The group ID
     * @return ResponseEntity with list of group members
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String groupId) {
        logger.info("Retrieving members for group: {}", groupId);

        try {
            List<String> members = groupChatService.getGroupMembers(groupId);
            logger.debug("Found {} members in group", members.size());
            
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            logger.error("Error retrieving group members: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to retrieve members: " + e.getMessage());
        }
    }

    /**
     * Leave a group.
     * 
     * @param groupId The group ID
     * @param userId The user ID who is leaving
     * @return ResponseEntity with success message
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(
            @PathVariable String groupId,
            @RequestParam String userId) {
        
        logger.info("User {} leaving group {}", userId, groupId);

        try {
            groupChatService.leaveGroup(groupId, userId);
            logger.debug("User left group successfully");
            
            return ResponseEntity.ok("Left group successfully");
        } catch (Exception e) {
            logger.error("Error leaving group: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to leave group: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for group chat service.
     * 
     * @return ResponseEntity with service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Group chat service health check");
        return ResponseEntity.ok("Group Chat Service is running");
    }
}
