package com.islandhop.userservices.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.islandhop.userservices.model.DriverAccount;
import com.islandhop.userservices.model.DriverProfile;
import com.islandhop.userservices.model.DriverVehicle;
import com.islandhop.userservices.repository.DriverAccountRepository;
import com.islandhop.userservices.repository.DriverProfileRepository;
import com.islandhop.userservices.repository.DriverVehicleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverService {

    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);

    private final DriverAccountRepository accountRepository;
    private final DriverProfileRepository profileRepository;
    private final DriverVehicleRepository vehicleRepository;
    private final DriverProfileRepository driverProfileRepository;

    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (Exception e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
    }

    public DriverAccount createDriverAccount(String email) {
        DriverAccount account = new DriverAccount();
        account.setEmail(email);
        account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }

    public DriverProfile createBasicDriverProfile(String email) {
        DriverProfile profile = DriverProfile.builder()
            .email(email)
            .profileCompletion(0)
            .build();
        DriverProfile savedProfile = profileRepository.save(profile);
        
        // Create a basic driver vehicle record with only email
        createBasicDriverVehicle(email);
        
        return savedProfile;
    }

    public DriverProfile updateDriverProfile(String email, Map<String, Object> requestBody) {
        DriverProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            throw new RuntimeException("Driver profile not found");
        }

        // Update personal information
        if (requestBody.containsKey("firstName")) {
            profile.setFirstName((String) requestBody.get("firstName"));
        }
        if (requestBody.containsKey("lastName")) {
            profile.setLastName((String) requestBody.get("lastName"));
        }
        if (requestBody.containsKey("phone")) {
            profile.setPhoneNumber((String) requestBody.get("phone"));
        }
        if (requestBody.containsKey("dateOfBirth")) {
            String dobStr = (String) requestBody.get("dateOfBirth");
            if (dobStr != null && !dobStr.trim().isEmpty()) {
                profile.setDateOfBirth(java.time.LocalDate.parse(dobStr));
            }
        }
        if (requestBody.containsKey("address")) {
            profile.setAddress((String) requestBody.get("address"));
        }
        if (requestBody.containsKey("emergencyContactName")) {
            profile.setEmergencyContactName((String) requestBody.get("emergencyContactName"));
        }
        if (requestBody.containsKey("emergencyContact")) {
            profile.setEmergencyContactNumber((String) requestBody.get("emergencyContact"));
        }
        if (requestBody.containsKey("profilePicture")) {
            profile.setProfilePictureUrl((String) requestBody.get("profilePicture"));
        }

        // Update trip preferences
        if (requestBody.containsKey("acceptPartialTrips")) {
            Object value = requestBody.get("acceptPartialTrips");
            if (value instanceof Integer) {
                profile.setAcceptPartialTrips((Integer) value);
            } else if (value instanceof Boolean) {
                profile.setAcceptPartialTrips(((Boolean) value) ? 1 : 0);
            }
        }
        if (requestBody.containsKey("autoAcceptTrips")) {
            Object value = requestBody.get("autoAcceptTrips");
            if (value instanceof Integer) {
                profile.setAutoAcceptTrips((Integer) value);
            } else if (value instanceof Boolean) {
                profile.setAutoAcceptTrips(((Boolean) value) ? 1 : 0);
            }
        }
        if (requestBody.containsKey("maxDistance")) {
            Object value = requestBody.get("maxDistance");
            if (value instanceof Integer) {
                profile.setMaximumTripDistance((Integer) value);
            } else if (value instanceof String) {
                try {
                    profile.setMaximumTripDistance(Integer.parseInt((String) value));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid maxDistance value: {}", value);
                }
            }
        }

        // Check if profile is complete
        if (isProfileComplete(profile)) {
            profile.setProfileCompletion(1);
        } else {
            profile.setProfileCompletion(0);
        }

        return profileRepository.save(profile);
    }

    private boolean isProfileComplete(DriverProfile profile) {
        return profile.getFirstName() != null && !profile.getFirstName().trim().isEmpty() &&
               profile.getLastName() != null && !profile.getLastName().trim().isEmpty() &&
               profile.getPhoneNumber() != null && !profile.getPhoneNumber().trim().isEmpty() &&
               profile.getDateOfBirth() != null &&
               profile.getAddress() != null && !profile.getAddress().trim().isEmpty() &&
               profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().trim().isEmpty() &&
               profile.getEmergencyContactNumber() != null && !profile.getEmergencyContactNumber().trim().isEmpty();
    }

    public DriverVehicle createBasicDriverVehicle(String email) {
        // Check if vehicle record already exists
        if (vehicleRepository.existsByEmail(email)) {
            return vehicleRepository.findByEmail(email).orElse(null);
        }
        
        DriverVehicle vehicle = DriverVehicle.builder()
            .email(email)
            .vehicleRegistrationStatus(2) // pending
            .insuranceCertificateStatus(2) // pending
            .build();
        return vehicleRepository.save(vehicle);
    }
    
    public DriverVehicle getDriverVehicle(String email) {
        return vehicleRepository.findByEmail(email).orElse(null);
    }
    
    public DriverVehicle updateDriverVehicle(String email, Map<String, Object> requestBody, Map<String, MultipartFile> files) {
        DriverVehicle vehicle = vehicleRepository.findByEmail(email).orElse(null);
        if (vehicle == null) {
            // Create if doesn't exist
            vehicle = createBasicDriverVehicle(email);
        }
        
        try {
            // Update text fields - handle both frontend and backend parameter names
            if (requestBody.containsKey("Fueltype") || requestBody.containsKey("fueltype")) {
                String value = (String) (requestBody.containsKey("Fueltype") ? 
                    requestBody.get("Fueltype") : requestBody.get("fueltype"));
                vehicle.setFueltype(value);
            }
            if (requestBody.containsKey("Capacity") || requestBody.containsKey("capacity")) {
                String value = (String) (requestBody.containsKey("Capacity") ? 
                    requestBody.get("Capacity") : requestBody.get("capacity"));
                vehicle.setCapacity(value);
            }
            if (requestBody.containsKey("Make") || requestBody.containsKey("make")) {
                String value = (String) (requestBody.containsKey("Make") ? 
                    requestBody.get("Make") : requestBody.get("make"));
                vehicle.setMake(value);
            }
            if (requestBody.containsKey("Model") || requestBody.containsKey("model")) {
                String value = (String) (requestBody.containsKey("Model") ? 
                    requestBody.get("Model") : requestBody.get("model"));
                vehicle.setModel(value);
            }
            if (requestBody.containsKey("Year") || requestBody.containsKey("year")) {
                String value = (String) (requestBody.containsKey("Year") ? 
                    requestBody.get("Year") : requestBody.get("year"));
                vehicle.setYear(value);
            }
            if (requestBody.containsKey("Color") || requestBody.containsKey("color")) {
                String value = (String) (requestBody.containsKey("Color") ? 
                    requestBody.get("Color") : requestBody.get("color"));
                vehicle.setColor(value);
            }
            if (requestBody.containsKey("Plate Number") || requestBody.containsKey("plateNumber")) {
                String value = (String) (requestBody.containsKey("Plate Number") ? 
                    requestBody.get("Plate Number") : requestBody.get("plateNumber"));
                vehicle.setPlateNumber(value);
            }
            if (requestBody.containsKey("Type") || requestBody.containsKey("type")) {
                String value = (String) (requestBody.containsKey("Type") ? 
                    requestBody.get("Type") : requestBody.get("type"));
                vehicle.setType(value);
            }

            if(requestBody.containsKey("Boot Capacity") || requestBody.containsKey("bootCapacity")) {
                String value = (String) (requestBody.containsKey("Boot Capacity") ?
                    requestBody.get("Boot Capacity") : requestBody.get("bootCapacity"));
                vehicle.setBootCapacity(value);
            }
            
            // Update image files - handle frontend parameter names
            if (files != null) {
                if (files.containsKey("Veh_pic_1") && !files.get("Veh_pic_1").isEmpty()) {
                    vehicle.setVehiclePic1(files.get("Veh_pic_1").getBytes());
                }
                if (files.containsKey("Veh_pic_2") && !files.get("Veh_pic_2").isEmpty()) {
                    vehicle.setVehiclePic2(files.get("Veh_pic_2").getBytes());
                }
                if (files.containsKey("Veh_pic_3") && !files.get("Veh_pic_3").isEmpty()) {
                    vehicle.setVehiclePic3(files.get("Veh_pic_3").getBytes());
                }
                if (files.containsKey("Veh_pic_4") && !files.get("Veh_pic_4").isEmpty()) {
                    vehicle.setVehiclePic4(files.get("Veh_pic_4").getBytes());
                }
                if (files.containsKey("Vehicle_registration(pic)") && !files.get("Vehicle_registration(pic)").isEmpty()) {
                    vehicle.setVehicleRegistrationPic(files.get("Vehicle_registration(pic)").getBytes());
                    vehicle.setVehicleRegistrationStatus(2); // pending verification
                }
                if (files.containsKey("Insurance Pic") && !files.get("Insurance Pic").isEmpty()) {
                    vehicle.setInsurancePic(files.get("Insurance Pic").getBytes());
                    vehicle.setInsuranceCertificateStatus(2); // pending verification
                }
            }
            
            // Update status fields
            if (requestBody.containsKey("Vehicle registration status")) {
                Object value = requestBody.get("Vehicle registration status");
                if (value instanceof String) {
                    vehicle.setVehicleRegistrationStatus(Integer.parseInt((String) value));
                } else if (value instanceof Integer) {
                    vehicle.setVehicleRegistrationStatus((Integer) value);
                }
            }
            if (requestBody.containsKey("Insurance certificate status")) {
                Object value = requestBody.get("Insurance certificate status");
                if (value instanceof String) {
                    vehicle.setInsuranceCertificateStatus(Integer.parseInt((String) value));
                } else if (value instanceof Integer) {
                    vehicle.setInsuranceCertificateStatus((Integer) value);
                }
            }
            
            return vehicleRepository.save(vehicle);
        } catch (Exception e) {
            logger.error("Error updating driver vehicle: {}", e.getMessage());
            throw new RuntimeException("Failed to update vehicle information");
        }
    }
    
    public Map<String, Object> getDriverVehicleWithImages(String email) {
        DriverVehicle vehicle = vehicleRepository.findByEmail(email).orElse(null);
        if (vehicle == null) {
            return null;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", vehicle.getId());
        response.put("email", vehicle.getEmail());
        // Use frontend-expected parameter names
        response.put("Fueltype", vehicle.getFueltype());
        response.put("Capacity", vehicle.getCapacity());
        response.put("Make", vehicle.getMake());
        response.put("Model", vehicle.getModel());
        response.put("Year", vehicle.getYear());
        response.put("Color", vehicle.getColor());
        response.put("Plate Number", vehicle.getPlateNumber());
        response.put("Type", vehicle.getType());
        response.put("Boot Capacity", vehicle.getBootCapacity());
        response.put("Vehicle registration status", vehicle.getVehicleRegistrationStatus());
        response.put("Insurance certificate status", vehicle.getInsuranceCertificateStatus());
        response.put("createdAt", vehicle.getCreatedAt());
        response.put("updatedAt", vehicle.getUpdatedAt());
        
        // Convert images to base64 with frontend-expected names
        if (vehicle.getVehiclePic1() != null) {
            response.put("Veh_pic_1", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getVehiclePic1()));
        }
        if (vehicle.getVehiclePic2() != null) {
            response.put("Veh_pic_2", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getVehiclePic2()));
        }
        if (vehicle.getVehiclePic3() != null) {
            response.put("Veh_pic_3", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getVehiclePic3()));
        }
        if (vehicle.getVehiclePic4() != null) {
            response.put("Veh_pic_4", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getVehiclePic4()));
        }
        if (vehicle.getVehicleRegistrationPic() != null) {
            response.put("Vehicle_registration(pic)", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getVehicleRegistrationPic()));
        }
        if (vehicle.getInsurancePic() != null) {
            response.put("Insurance Pic", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(vehicle.getInsurancePic()));
        }
        
        return response;
    }


    public void uploadDrivingLicense(String email, MultipartFile file) {

        DriverProfile driver = driverProfileRepository.findByEmail(email);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }

        try {
            if (file != null && !file.isEmpty()) {
                // Convert file to base64 string for TEXT column storage
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                driver.setDrivingLicenseImage(base64Image);
                driver.setDrivingLicenseVerified(0); // pending verification
                driverProfileRepository.save(driver);
            } else {
                throw new RuntimeException("File is empty or not provided");
            }
        } catch (Exception e) {
            logger.error("Error uploading driving license: {}", e.getMessage());
            throw new RuntimeException("Failed to upload driving license");
        }
    }

    public void uploadSltdaLicense(String email, MultipartFile file) {
        DriverProfile driver = driverProfileRepository.findByEmail(email);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }

        try {
            if (file != null && !file.isEmpty()) {
                // Convert file to base64 string for TEXT column storage
                String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                driver.setSltdaLicenseImage(base64Image);
                driver.setSltdaLicenseVerified(0); // pending verification
                driverProfileRepository.save(driver);
            } else {
                throw new RuntimeException("File is empty or not provided");
            }
        } catch (Exception e) {
            logger.error("Error uploading SLTDA license: {}", e.getMessage());
            throw new RuntimeException("Failed to upload SLTDA license");
        }
    }

}

