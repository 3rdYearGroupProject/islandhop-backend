package com.islandhop.pooling.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MongoDB entity representing a travel group.
 * Stores group metadata and links to TripPlan via tripId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "groups")
public class Group {
    
    @Id
    private String groupId;
    
    private String groupName;
    
    @Indexed
    private String tripId;
    
    private List<String> userIds = new ArrayList<>();
    
    @Indexed
    private String visibility; // "private" or "public"
    
    @Indexed
    private Map<String, Object> preferences;
    
    private List<JoinRequest> joinRequests = new ArrayList<>();
    
    private List<GroupAction> actions = new ArrayList<>();
    
    private Instant createdAt;
    
    private Instant lastUpdated;
    
    /**
     * Get the creator's user ID (first user in the list).
     */
    public String getCreatorUserId() {
        return userIds.isEmpty() ? null : userIds.get(0);
    }
    
    /**
     * Check if a user is a member of this group.
     */
    public boolean isMember(String userId) {
        return userIds.contains(userId);
    }
    
    /**
     * Check if a user is the creator of this group.
     */
    public boolean isCreator(String userId) {
        return userId != null && userId.equals(getCreatorUserId());
    }
    
    /**
     * Add a user to the group.
     */
    public void addUser(String userId) {
        if (!userIds.contains(userId)) {
            userIds.add(userId);
        }
    }
    
    /**
     * Remove a user from the group.
     */
    public void removeUser(String userId) {
        userIds.remove(userId);
    }
    
    /**
     * Check if the group is public.
     */
    public boolean isPublic() {
        return "public".equals(visibility);
    }
    
    /**
     * Check if the group is private.
     */
    public boolean isPrivate() {
        return "private".equals(visibility);
    }
}
