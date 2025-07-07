package com.islandhop.userservices.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.islandhop.userservices.dto.GuideCertificateDTO;
import com.islandhop.userservices.dto.GuideProfileDTO;
import com.islandhop.userservices.model.GuideAccount;
import com.islandhop.userservices.model.GuideCertificate;
import com.islandhop.userservices.model.GuideLanguage;
import com.islandhop.userservices.model.GuideProfile;
import com.islandhop.userservices.repository.GuideAccountRepository;
import com.islandhop.userservices.repository.GuideCertificateRepository;
import com.islandhop.userservices.repository.GuideLanguageRepository;
import com.islandhop.userservices.repository.GuideProfileRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideService {

    private static final Logger logger = LoggerFactory.getLogger(GuideService.class);

    private final GuideAccountRepository accountRepository;
    private final GuideProfileRepository profileRepository;
    private final GuideCertificateRepository certificateRepository;
    private final GuideLanguageRepository languageRepository;

    public String getEmailFromIdToken(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return decodedToken.getEmail();
        } catch (Exception e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage());
            return null;
        }
    }

    public GuideAccount createGuideAccount(String email) {
        GuideAccount account = GuideAccount.builder()
            .email(email)
            .status("ACTIVE")
            .build();
        return accountRepository.save(account);
    }

    public GuideProfile createBasicGuideProfile(String email) {
        GuideProfile profile = GuideProfile.builder()
            .email(email)
            .profileCompletion(0)
            .build();
        return profileRepository.save(profile);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public GuideProfile updateGuideProfile(String email, Map<String, Object> requestBody) {
        GuideProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            throw new RuntimeException("Guide profile not found");
        }

        // Update basic profile fields
        if (requestBody.containsKey("firstName")) {
            profile.setFirstName((String) requestBody.get("firstName"));
        }
        if (requestBody.containsKey("lastName")) {
            profile.setLastName((String) requestBody.get("lastName"));
        }
        if (requestBody.containsKey("phoneNumber")) {
            profile.setPhoneNumber((String) requestBody.get("phoneNumber"));
        }
        if (requestBody.containsKey("dateOfBirth")) {
            String dobString = (String) requestBody.get("dateOfBirth");
            if (dobString != null && !dobString.isEmpty()) {
                profile.setDateOfBirth(LocalDate.parse(dobString));
            }
        }
        if (requestBody.containsKey("address")) {
            profile.setAddress((String) requestBody.get("address"));
        }
        if (requestBody.containsKey("emergencyContactNumber")) {
            profile.setEmergencyContactNumber((String) requestBody.get("emergencyContactNumber"));
        }
        if (requestBody.containsKey("emergencyContactName")) {
            profile.setEmergencyContactName((String) requestBody.get("emergencyContactName"));
        }
        if (requestBody.containsKey("profilePictureBase64")) {
            String base64Image = (String) requestBody.get("profilePictureBase64");
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    profile.setProfilePicture(imageBytes);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid base64 image data for profile picture");
                }
            }
        }

        // Check if profile is complete
        if (isProfileComplete(profile)) {
            profile.setProfileCompletion(1);
        }

        return profileRepository.save(profile);
    }

    public GuideProfileDTO getGuideProfileDTO(String email) {
        GuideProfile profile = profileRepository.findByEmail(email);
        if (profile == null) {
            return null;
        }
        return GuideProfileDTO.fromEntity(profile);
    }

    // Certificate management methods
    @Transactional
    public GuideCertificate saveCertificate(String email, Map<String, Object> certData) {
        GuideCertificate certificate = GuideCertificate.builder()
            .email(email)
            .certificateId((String) certData.get("certificateId"))
            .certificateIssuer((String) certData.get("certificateIssuer"))
            .issueDate(LocalDate.parse((String) certData.get("issueDate")))
            .verificationNumber((String) certData.get("verificationNumber"))
            .build();

        if (certData.containsKey("expiryDate") && certData.get("expiryDate") != null) {
            certificate.setExpiryDate(LocalDate.parse((String) certData.get("expiryDate")));
        }

        if (certData.containsKey("certificatePictureBase64")) {
            String base64Image = (String) certData.get("certificatePictureBase64");
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    certificate.setCertificatePicture(imageBytes);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid base64 image data for certificate");
                }
            }
        }

        return certificateRepository.save(certificate);
    }

    public List<GuideCertificateDTO> getCertificates(String email) {
        List<GuideCertificate> certificates = certificateRepository.findByEmail(email);
        return certificates.stream()
            .map(GuideCertificateDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<GuideCertificateDTO> updateCertificates(String email, List<Map<String, Object>> certificatesData) {
        // Delete existing certificates
        certificateRepository.deleteByEmail(email);
        
        // Save new certificates
        List<GuideCertificate> certificates = certificatesData.stream()
            .map(certData -> {
                GuideCertificate certificate = GuideCertificate.builder()
                    .email(email)
                    .certificateId((String) certData.get("certificateId"))
                    .certificateIssuer((String) certData.get("certificateIssuer"))
                    .issueDate(LocalDate.parse((String) certData.get("issueDate")))
                    .verificationNumber((String) certData.get("verificationNumber"))
                    .build();

                if (certData.containsKey("expiryDate") && certData.get("expiryDate") != null) {
                    certificate.setExpiryDate(LocalDate.parse((String) certData.get("expiryDate")));
                }

                if (certData.containsKey("status")) {
                    certificate.setStatus(GuideCertificate.CertificateStatus.valueOf((String) certData.get("status")));
                }

                if (certData.containsKey("certificatePictureBase64")) {
                    String base64Image = (String) certData.get("certificatePictureBase64");
                    if (base64Image != null && !base64Image.isEmpty()) {
                        try {
                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                            certificate.setCertificatePicture(imageBytes);
                        } catch (IllegalArgumentException e) {
                            logger.error("Invalid base64 image data for certificate");
                        }
                    }
                }

                return certificate;
            })
            .collect(Collectors.toList());

        List<GuideCertificate> savedCertificates = certificateRepository.saveAll(certificates);
        return savedCertificates.stream()
            .map(GuideCertificateDTO::fromEntity)
            .collect(Collectors.toList());
    }

    // Language management methods
    public List<GuideLanguage> getLanguages(String email) {
        return languageRepository.findByEmail(email);
    }

    @Transactional
    public List<GuideLanguage> updateLanguages(String email, List<Map<String, Object>> languagesData) {
        // Delete existing languages
        languageRepository.deleteByEmail(email);
        
        // Save new languages
        List<GuideLanguage> languages = languagesData.stream()
            .map(langData -> GuideLanguage.builder()
                .email(email)
                .language((String) langData.get("language"))
                .level((String) langData.get("level"))
                .build())
            .collect(Collectors.toList());

        return languageRepository.saveAll(languages);
    }

    private boolean isProfileComplete(GuideProfile profile) {
        return profile.getFirstName() != null && !profile.getFirstName().trim().isEmpty() &&
               profile.getLastName() != null && !profile.getLastName().trim().isEmpty() &&
               profile.getPhoneNumber() != null && !profile.getPhoneNumber().trim().isEmpty() &&
               profile.getAddress() != null && !profile.getAddress().trim().isEmpty() &&
               profile.getEmergencyContactNumber() != null && !profile.getEmergencyContactNumber().trim().isEmpty() &&
               profile.getEmergencyContactName() != null && !profile.getEmergencyContactName().trim().isEmpty();
    }
}