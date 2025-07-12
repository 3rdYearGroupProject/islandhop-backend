package com.islandhop.userservices.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;
import com.islandhop.userservices.model.*;
import com.islandhop.userservices.repository.*;
import com.islandhop.userservices.dto.UserAccountResponse;
import com.islandhop.userservices.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Service implementation for user management operations
 * Handles user validation, retrieval, and status updates
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final TouristAccountRepository touristAccountRepository;
    private final DriverAccountRepository driverAccountRepository;
    private final GuideAccountRepository guideAccountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final SupportAccountRepository supportAccountRepository;
    
    // Profile repositories
    private final TouristProfileRepository touristProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final GuideProfileRepository guideProfileRepository;
    private final SupportProfileRepository supportProfileRepository;

    /**
     * Validates the Firebase ID token and returns user details (role, email, etc.) if valid.
     * Returns null if invalid or not found.
     */
    @Override
    public Map<String, Object> validateAndGetUserDetails(String idToken) {
        logger.info("Validating Firebase ID token");
        
        try {
            // Validate input
            if (idToken == null || idToken.trim().isEmpty()) {
                logger.warn("Empty or null ID token provided");
                return null;
            }
            
            // Verify Firebase ID token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            
            if (email == null) {
                logger.warn("No email found in Firebase token");
                return null;
            }

            // Check each user table for the email
            if (touristAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "tourist");
                details.put("email", email);
                
                // Get profile completion status
                TouristProfile profile = touristProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            
            if (driverAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "driver");
                details.put("email", email);
                
                // Get profile completion status
                DriverProfile profile = driverProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            
            if (guideAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "guide");
                details.put("email", email);
                
                // Get profile completion status
                GuideProfile profile = guideProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            
            if (adminAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "admin");
                details.put("email", email);
                return details;
            }
            
            if (supportAccountRepository.existsByEmail(email)) {
                Map<String, Object> details = new HashMap<>();
                details.put("role", "support");
                details.put("email", email);
                
                // Get profile completion status
                SupportProfile profile = supportProfileRepository.findByEmail(email);
                boolean profileComplete = profile != null && profile.getProfileCompletion() == 1;
                details.put("profileComplete", profileComplete);
                
                return details;
            }
            
            // Not found in any table
            logger.warn("User not found in any account table for email: {}", email);
            return null;
            
        } catch (FirebaseAuthException e) {
            logger.error("Firebase authentication error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error validating Firebase ID token", e);
            return null;
        }
    }

    /**
     * Get all users in the system
     * @return List of user account responses containing user details
     */
    @Override
    public List<UserAccountResponse> getAllUsers() {
        logger.info("Fetching all users from the system");
        
        try {
            List<UserAccountResponse> users = new ArrayList<>();
            
            // Get all tourists
            List<TouristAccount> tourists = touristAccountRepository.findAll();
            for (TouristAccount tourist : tourists) {
                TouristProfile profile = touristProfileRepository.findByEmail(tourist.getEmail());
                users.add(new UserAccountResponse(
                    getFirstName(profile),
                    getLastName(profile),
                    tourist.getEmail(),
                    getProfilePicUrl(profile),
                    "TOURIST",
                    getAccountStatus(tourist.getStatus())
                ));
            }
            
            // Get all drivers
            List<DriverAccount> drivers = driverAccountRepository.findAll();
            for (DriverAccount driver : drivers) {
                DriverProfile profile = driverProfileRepository.findByEmail(driver.getEmail());
                users.add(new UserAccountResponse(
                    getFirstName(profile),
                    getLastName(profile),
                    driver.getEmail(),
                    getProfilePicUrl(profile),
                    "DRIVER",
                    getAccountStatus(driver.getStatus())
                ));
            }
            
            // Get all guides
            List<GuideAccount> guides = guideAccountRepository.findAll();
            for (GuideAccount guide : guides) {
                GuideProfile profile = guideProfileRepository.findByEmail(guide.getEmail());
                users.add(new UserAccountResponse(
                    getFirstName(profile),
                    getLastName(profile),
                    guide.getEmail(),
                    getProfilePicUrl(profile),
                    "GUIDE",
                    getAccountStatus(guide.getStatus())
                ));
            }
            
            // Get all support accounts
            List<SupportAccount> supportAccounts = supportAccountRepository.findAll();
            for (SupportAccount support : supportAccounts) {
                SupportProfile profile = supportProfileRepository.findByEmail(support.getEmail());
                users.add(new UserAccountResponse(
                    getFirstName(profile),
                    getLastName(profile),
                    support.getEmail(),
                    getProfilePicUrl(profile),
                    "SUPPORT",
                    getAccountStatus(support.getStatus())
                ));
            }
            
            // Get all admin accounts
            List<AdminAccount> adminAccounts = adminAccountRepository.findAll();
            for (AdminAccount admin : adminAccounts) {
                users.add(new UserAccountResponse(
                    getAdminFirstName(admin),
                    getAdminLastName(admin),
                    admin.getEmail(),
                    getAdminProfilePicUrl(admin),
                    "ADMIN",
                    getAccountStatus(admin.getStatus())
                ));
            }
            
            logger.info("Successfully fetched {} users", users.size());
            return users;
            
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    /**
     * Update user account status
     * @param email User email to update
     * @param status New status to set
     * @throws IllegalArgumentException if user not found or invalid status
     */
    @Override
    public void updateUserStatus(String email, String status) {
        logger.info("Updating user status for email: {} to status: {}", email, status);
        
        // Validate input parameters
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        // Validate status value
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Must be one of: ACTIVE, DEACTIVATED, SUSPENDED, PENDING");
        }
        
        try {
            boolean userFound = false;
            
            // Check and update tourist account
            if (touristAccountRepository.existsByEmail(email)) {
                TouristAccount tourist = touristAccountRepository.findByEmail(email);
                if (tourist != null) {
                    setAccountStatus(tourist, status);
                    tourist.setLastUpdated(Instant.now());
                    touristAccountRepository.save(tourist);
                    userFound = true;
                }
            }
            
            // Check and update driver account
            if (!userFound && driverAccountRepository.existsByEmail(email)) {
                DriverAccount driver = driverAccountRepository.findByEmail(email);
                if (driver != null) {
                    setAccountStatus(driver, status);
                    driver.setLastUpdated(Instant.now());
                    driverAccountRepository.save(driver);
                    userFound = true;
                }
            }
            
            // Check and update guide account
            if (!userFound && guideAccountRepository.existsByEmail(email)) {
                GuideAccount guide = guideAccountRepository.findByEmail(email);
                if (guide != null) {
                    setAccountStatus(guide, status);
                    guide.setLastUpdated(Instant.now());
                    guideAccountRepository.save(guide);
                    userFound = true;
                }
            }
            
            // Check and update support account
            if (!userFound && supportAccountRepository.existsByEmail(email)) {
                SupportAccount support = supportAccountRepository.findByEmail(email);
                if (support != null) {
                    setAccountStatus(support, status);
                    support.setLastUpdated(Instant.now());
                    supportAccountRepository.save(support);
                    userFound = true;
                }
            }
            
            // Check and update admin account
            if (!userFound && adminAccountRepository.existsByEmail(email)) {
                AdminAccount admin = adminAccountRepository.findByEmail(email);
                if (admin != null) {
                    setAccountStatus(admin, status);
                    admin.setLastUpdated(Instant.now());
                    adminAccountRepository.save(admin);
                    userFound = true;
                }
            }
            
            if (!userFound) {
                throw new IllegalArgumentException("User not found with email: " + email);
            }
            
            logger.info("Successfully updated status for user: {} to: {}", email, status);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for status update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user status for email: {}", email, e);
            throw new RuntimeException("Failed to update user status", e);
        }
    }

    /**
     * Validate if the provided status is valid
     * @param status Status to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidStatus(String status) {
        List<String> validStatuses = Arrays.asList("ACTIVE", "DEACTIVATED", "SUSPENDED", "PENDING");
        return status != null && validStatuses.contains(status.toUpperCase());
    }

    /**
     * Safe getter for first name from profile objects
     * Uses reflection to handle different profile types
     */
    private String getFirstName(Object profile) {
        if (profile == null) return "";
        
        try {
            var method = profile.getClass().getMethod("getFirstName");
            Object result = method.invoke(profile);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get firstName from profile: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for last name from profile objects
     * Uses reflection to handle different profile types
     */
    private String getLastName(Object profile) {
        if (profile == null) return "";
        
        try {
            var method = profile.getClass().getMethod("getLastName");
            Object result = method.invoke(profile);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get lastName from profile: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for profile picture URL from profile objects
     * Uses reflection to handle different profile types
     */
    private String getProfilePicUrl(Object profile) {
        if (profile == null) return "";
        
        try {
            var method = profile.getClass().getMethod("getProfilePicUrl");
            Object result = method.invoke(profile);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get profilePicUrl from profile: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for admin first name
     */
    private String getAdminFirstName(AdminAccount admin) {
        if (admin == null) return "";
        
        try {
            var method = admin.getClass().getMethod("getFirstName");
            Object result = method.invoke(admin);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get firstName from admin: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for admin last name
     */
    private String getAdminLastName(AdminAccount admin) {
        if (admin == null) return "";
        
        try {
            var method = admin.getClass().getMethod("getLastName");
            Object result = method.invoke(admin);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get lastName from admin: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for admin profile picture URL
     */
    private String getAdminProfilePicUrl(AdminAccount admin) {
        if (admin == null) return "";
        
        try {
            var method = admin.getClass().getMethod("getProfilePicUrl");
            Object result = method.invoke(admin);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            logger.debug("Could not get profilePicUrl from admin: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe getter for account status - handles both String and Enum types
     */
    private String getAccountStatus(Object status) {
        if (status == null) return "ACTIVE";
        
        if (status instanceof String) {
            return (String) status;
        } else if (status instanceof Enum) {
            return ((Enum<?>) status).name();
        } else {
            return status.toString();
        }
    }

    /**
     * Safe setter for account status - handles both String and Enum types using reflection
     */
    private void setAccountStatus(Object account, String status) {
        if (account == null) return;
        
        try {
            // First try to find setStatus method with String parameter
            try {
                var method = account.getClass().getMethod("setStatus", String.class);
                method.invoke(account, status);
                return;
            } catch (NoSuchMethodException e) {
                // Method with String parameter doesn't exist, try with enum
            }
            
            // Try to find setStatus method with enum parameter
            var methods = account.getClass().getMethods();
            for (var method : methods) {
                if ("setStatus".equals(method.getName()) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (paramType.isEnum()) {
                        // Convert string to enum
                        Object enumValue = Enum.valueOf((Class<Enum>) paramType, status.toUpperCase());
                        method.invoke(account, enumValue);
                        return;
                    }
                }
            }
            
            logger.warn("Could not find setStatus method for account: {}", account.getClass().getSimpleName());
            
        } catch (Exception e) {
            logger.error("Error setting account status: {}", e.getMessage());
            throw new RuntimeException("Failed to set account status", e);
        }
    }
}