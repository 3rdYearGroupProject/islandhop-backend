package com.islandhop.userservices.dto;

import com.islandhop.userservices.model.GuideProfile;
import lombok.Data;

import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Data
public class GuideProfileDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String emergencyContactNumber;
    private String emergencyContactName;
    private String profilePictureBase64;
    private Integer profileCompletion;
    
    public static GuideProfileDTO fromEntity(GuideProfile profile) {
        GuideProfileDTO dto = new GuideProfileDTO();
        dto.setId(profile.getId());
        dto.setEmail(profile.getEmail());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setAddress(profile.getAddress());
        dto.setEmergencyContactNumber(profile.getEmergencyContactNumber());
        dto.setEmergencyContactName(profile.getEmergencyContactName());
        dto.setProfileCompletion(profile.getProfileCompletion());
        
        if (profile.getProfilePicture() != null) {
            dto.setProfilePictureBase64(Base64.getEncoder().encodeToString(profile.getProfilePicture()));
        }
        
        return dto;
    }
}
