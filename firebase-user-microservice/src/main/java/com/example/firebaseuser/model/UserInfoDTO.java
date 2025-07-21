package com.example.firebaseuser.model;

/**
 * DTO for Firebase user info response.
 */
public class UserInfoDTO {
    private String uid;
    private String email;
    private String displayName;
    private boolean disabled;
    private String phoneNumber;
    private String photoUrl;
    private long creationTimestamp;
    private long lastSignInTimestamp;

    public UserInfoDTO() {}

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isDisabled() { return disabled; }
    public void setDisabled(boolean disabled) { this.disabled = disabled; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public long getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(long creationTimestamp) { this.creationTimestamp = creationTimestamp; }
    public long getLastSignInTimestamp() { return lastSignInTimestamp; }
    public void setLastSignInTimestamp(long lastSignInTimestamp) { this.lastSignInTimestamp = lastSignInTimestamp; }
}