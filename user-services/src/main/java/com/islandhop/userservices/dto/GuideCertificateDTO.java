package com.islandhop.userservices.dto;

import com.islandhop.userservices.model.GuideCertificate;
import lombok.Data;

import java.time.LocalDate;
import java.util.Base64;

@Data
public class GuideCertificateDTO {
    private Long id;
    private String email;
    private String certificateId;
    private String certificateIssuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String verificationNumber;
    private String certificatePictureBase64;
    private GuideCertificate.CertificateStatus status;
    
    public static GuideCertificateDTO fromEntity(GuideCertificate certificate) {
        GuideCertificateDTO dto = new GuideCertificateDTO();
        dto.setId(certificate.getId());
        dto.setEmail(certificate.getEmail());
        dto.setCertificateId(certificate.getCertificateId());
        dto.setCertificateIssuer(certificate.getCertificateIssuer());
        dto.setIssueDate(certificate.getIssueDate());
        dto.setExpiryDate(certificate.getExpiryDate());
        dto.setVerificationNumber(certificate.getVerificationNumber());
        dto.setStatus(certificate.getStatus());
        
        if (certificate.getCertificatePicture() != null) {
            dto.setCertificatePictureBase64(Base64.getEncoder().encodeToString(certificate.getCertificatePicture()));
        }
        
        return dto;
    }
}
