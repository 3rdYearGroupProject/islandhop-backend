package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guide_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuideCertificate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String certificateId;
    
    @Column(nullable = false)
    private String certificateIssuer;
    
    @Column(nullable = false)
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    private String verificationNumber;
    
    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] certificatePicture;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.PENDING;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum CertificateStatus {
        ACTIVE, PENDING, REJECTED
    }
}
