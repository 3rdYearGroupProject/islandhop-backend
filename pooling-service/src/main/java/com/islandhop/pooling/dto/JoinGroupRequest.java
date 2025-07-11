package com.islandhop.pooling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for joining a public group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequest {
    
    @NotNull(message = "User profile is required")
    private Map<String, Object> userProfile;
    
    /**
     * Convenience methods for accessing common profile fields.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getTravelDates() {
        return (Map<String, String>) userProfile.get("travelDates");
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getInterests() {
        return (List<String>) userProfile.get("interests");
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getLanguage() {
        return (List<String>) userProfile.get("language");
    }
    
    public String getBudgetLevel() {
        return (String) userProfile.get("budgetLevel");
    }
    
    public String getAgeRange() {
        return (String) userProfile.get("ageRange");
    }
}
