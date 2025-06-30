package com.islandhop.pooling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoolMember {
    
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String nationality;
    private List<String> languages;
    
    // Trip Information
    private String tripId;
    private String tripName;
    private List<String> tripCategories;
    private String activityPacing; // RELAXED, NORMAL, ACTIVE
    
    // Pool Participation
    private MemberRole role;
    private MemberStatus status;
    private LocalDateTime joinedAt;
    private Double compatibilityScore;
    
    // Preferences
    private List<String> sharedInterests;
    private boolean willingToShareTransport;
    private boolean willingToShareAccommodation;
    private String communicationPreference; // CHAT, PHONE, EMAIL
    
    public enum MemberRole {
        CREATOR,    // Pool creator
        MEMBER,     // Regular member
        MODERATOR   // Has admin privileges
    }
    
    public enum MemberStatus {
        PENDING,    // Waiting for approval
        ACTIVE,     // Active member
        LEFT,       // Left the pool
        REMOVED     // Removed by moderator
    }
}
