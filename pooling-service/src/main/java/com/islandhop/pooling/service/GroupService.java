package com.islandhop.pooling.service;

import com.islandhop.pooling.dto.*;
import com.islandhop.pooling.exception.*;
import com.islandhop.pooling.model.Group;
import com.islandhop.pooling.model.GroupAction;
import com.islandhop.pooling.model.JoinRequest;
import com.islandhop.pooling.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing groups and their operations.
 * Follows the same patterns as TripService for consistency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    
    /**
     * Creates a new travel group.
     * Validates input and initializes group with default settings.
     *
     * @param request The group creation request
     * @return CreateGroupResponse with group details
     * @throws GroupCreationException if validation fails
     */
    public CreateGroupResponse createGroup(CreateGroupRequest request) {
        log.info("Creating group '{}' for user {}", request.getGroupName(), request.getUserId());
        
        try {
            // Validate input
            validateCreateGroupRequest(request);
            
            // Generate group ID
            String groupId = UUID.randomUUID().toString();
            
            // Create group entity
            Group group = new Group();
            group.setId(groupId);
            group.setGroupName(request.getGroupName());
            group.setTripId(request.getTripId());
            group.setVisibility(request.getVisibility());
            group.setPreferences(request.getPreferences());
            group.setUserIds(List.of(request.getUserId()));
            group.setCreatedAt(Instant.now());
            group.setLastUpdated(Instant.now());
            
            // Add creation action
            GroupAction createAction = GroupAction.create(
                request.getUserId(),
                "GROUP_CREATED",
                "Group created with name: " + request.getGroupName()
            );
            group.setActions(List.of(createAction));
            
            // Save group
            Group savedGroup = groupRepository.save(group);
            
            // Create response
            CreateGroupResponse response = new CreateGroupResponse();
            response.setStatus("success");
            response.setGroupId(savedGroup.getId());
            response.setGroupName(savedGroup.getGroupName());
            response.setTripId(savedGroup.getTripId());
            response.setMessage("Group created successfully");
            
            log.info("Group '{}' created successfully with ID: {}", request.getGroupName(), groupId);
            return response;
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid group creation request: {}", e.getMessage());
            throw new GroupCreationException("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating group: {}", e.getMessage(), e);
            throw new GroupCreationException("Failed to create group: " + e.getMessage());
        }
    }
    
    /**
     * Invites a user to a private group.
     */
    public InviteUserResponse inviteUser(String groupId, InviteUserRequest request) {
        log.info("Inviting user '{}' to group '{}'", request.getInvitedUserId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Validate permissions
            if (!group.isMember(request.getUserId())) {
                throw new UnauthorizedGroupAccessException("User is not a member of this group");
            }
            
            if (group.isPublic()) {
                throw new InvalidGroupOperationException("Cannot invite users to public groups");
            }
            
            // Check if user is already a member
            if (group.isMember(request.getInvitedUserId())) {
                throw new InvalidGroupOperationException("User is already a member of this group");
            }
            
            // Add user to group
            group.addUser(request.getInvitedUserId());
            group.setLastUpdated(Instant.now());
            
            // Add action
            GroupAction inviteAction = GroupAction.create(
                request.getUserId(),
                "USER_INVITED",
                "User invited: " + request.getInvitedUserId()
            );
            group.getActions().add(inviteAction);
            
            // Save group
            groupRepository.save(group);
            
            // Create response
            InviteUserResponse response = new InviteUserResponse();
            response.setStatus("success");
            response.setGroupId(groupId);
            response.setInvitedUserId(request.getInvitedUserId());
            response.setMessage("User invited successfully");
            
            log.info("User '{}' invited to group '{}' successfully", request.getInvitedUserId(), groupId);
            return response;
            
        } catch (GroupNotFoundException | UnauthorizedGroupAccessException | InvalidGroupOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error inviting user to group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to invite user: " + e.getMessage());
        }
    }
    
    /**
     * Requests to join a public group.
     */
    public JoinGroupResponse joinGroup(String groupId, JoinGroupRequest request) {
        log.info("User '{}' requesting to join group '{}'", request.getUserId(), groupId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Validate group is public
            if (!group.isPublic()) {
                throw new InvalidGroupOperationException("Cannot join private groups");
            }
            
            // Check if user is already a member
            if (group.isMember(request.getUserId())) {
                throw new InvalidGroupOperationException("User is already a member of this group");
            }
            
            // For public groups, add user directly or create join request
            // For simplicity, adding user directly here
            group.addUser(request.getUserId());
            group.setLastUpdated(Instant.now());
            
            // Add action
            GroupAction joinAction = GroupAction.create(
                request.getUserId(),
                "USER_JOINED",
                "User joined the group"
            );
            group.getActions().add(joinAction);
            
            // Save group
            groupRepository.save(group);
            
            // Create response
            JoinGroupResponse response = new JoinGroupResponse();
            response.setStatus("success");
            response.setGroupId(groupId);
            response.setMessage("Successfully joined the group");
            
            log.info("User '{}' joined group '{}' successfully", request.getUserId(), groupId);
            return response;
            
        } catch (GroupNotFoundException | InvalidGroupOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error joining group {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to join group: " + e.getMessage());
        }
    }
    
    /**
     * Gets group details.
     */
    public GroupDetailsResponse getGroupDetails(String groupId, String userId) {
        log.info("Getting group details for '{}' by user '{}'", groupId, userId);
        
        try {
            // Find group
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + groupId));
            
            // Check permissions
            if (!group.isMember(userId) && !group.isPublic()) {
                throw new UnauthorizedGroupAccessException("User is not authorized to view this group");
            }
            
            // Create response
            GroupDetailsResponse response = new GroupDetailsResponse();
            response.setGroupId(group.getId());
            response.setGroupName(group.getGroupName());
            response.setTripId(group.getTripId());
            response.setVisibility(group.getVisibility());
            response.setUserIds(group.getUserIds());
            response.setPreferences(group.getPreferences());
            response.setCreatedAt(group.getCreatedAt());
            response.setLastUpdated(group.getLastUpdated());
            
            log.info("Group details retrieved for '{}' successfully", groupId);
            return response;
            
        } catch (GroupNotFoundException | UnauthorizedGroupAccessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting group details for {}: {}", groupId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get group details: " + e.getMessage());
        }
    }
    
    /**
     * Gets list of public groups.
     */
    public List<PublicGroupResponse> getPublicGroups(String userId) {
        log.info("Getting public groups for user '{}'", userId);
        
        try {
            // Find public groups
            List<Group> publicGroups = groupRepository.findByVisibility("public");
            
            // Convert to response DTOs
            List<PublicGroupResponse> responses = publicGroups.stream()
                .map(this::convertToPublicGroupResponse)
                .collect(Collectors.toList());
            
            log.info("Found {} public groups for user '{}'", responses.size(), userId);
            return responses;
            
        } catch (Exception e) {
            log.error("Unexpected error getting public groups for user {}: {}", userId, e.getMessage(), e);
            throw new GroupCreationException("Failed to get public groups: " + e.getMessage());
        }
    }
    
    /**
     * Validates create group request.
     */
    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        
        if (request.getVisibility() == null) {
            request.setVisibility("private");
        }
        
        if (!Arrays.asList("private", "public").contains(request.getVisibility())) {
            throw new IllegalArgumentException("Visibility must be 'private' or 'public'");
        }
    }
    
    /**
     * Converts Group entity to PublicGroupResponse DTO.
     */
    private PublicGroupResponse convertToPublicGroupResponse(Group group) {
        PublicGroupResponse response = new PublicGroupResponse();
        response.setGroupId(group.getId());
        response.setGroupName(group.getGroupName());
        response.setTripId(group.getTripId());
        response.setPreferences(group.getPreferences());
        response.setMemberCount(group.getUserIds().size());
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }
}