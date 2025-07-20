package com.islandhop.userservices.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverVehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    // Vehicle Basic Information
    private String fueltype;
    private String capacity;
    private String make;
    private String model;
    private String year;
    private String color;
    private String bootCapacity;
    
    @Column(name = "plate_number")
    private String plateNumber;
    
    private String type;
    
    // Vehicle Pictures (stored as binary data)
    @Column(name = "veh_pic_1")
    private byte[] vehiclePic1;
    
    @Column(name = "veh_pic_2")
    private byte[] vehiclePic2;
    
    @Column(name = "veh_pic_3")
    private byte[] vehiclePic3;
    
    @Column(name = "veh_pic_4")
    private byte[] vehiclePic4;
    
    // Document Pictures
    @Column(name = "vehicle_registration_pic")
    private byte[] vehicleRegistrationPic;
    
    @Column(name = "insurance_pic")
    private byte[] insurancePic;
    
    // Document Status Fields
    @Builder.Default
    @Column(name = "vehicle_registration_status")
    private Integer vehicleRegistrationStatus = 2; // 1=verified, 2=pending, 3=rejected
    
    @Builder.Default
    @Column(name = "insurance_certificate_status")
    private Integer insuranceCertificateStatus = 2; // 1=verified, 2=pending, 3=rejected
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
