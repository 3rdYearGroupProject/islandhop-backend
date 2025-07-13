package com.islandhop.pooling.repository;

import com.islandhop.pooling.model.Invitation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing invitations.
 */
@Repository
public interface InvitationRepository extends MongoRepository<Invitation, String> {
    
    /**
     * Find pending invitations for a specific user.
     */
    @Query("{ 'invitedUserId': ?0, 'status': 'pending' }")
    List<Invitation> findPendingInvitationsByUserId(String userId);
    
    /**
     * Find pending invitations by email.
     */
    @Query("{ 'invitedEmail': ?0, 'status': 'pending' }")
    List<Invitation> findPendingInvitationsByEmail(String email);
    
    /**
     * Find all invitations for a specific group.
     */
    List<Invitation> findByGroupId(String groupId);
    
    /**
     * Find invitation by group and invited user.
     */
    Optional<Invitation> findByGroupIdAndInvitedUserId(String groupId, String invitedUserId);
    
    /**
     * Find expired invitations.
     */
    @Query("{ 'expiresAt': { $lt: ?0 }, 'status': 'pending' }")
    List<Invitation> findExpiredInvitations(Instant now);
    
    /**
     * Find invitations sent by a specific user.
     */
    List<Invitation> findByInviterUserId(String inviterUserId);
}
