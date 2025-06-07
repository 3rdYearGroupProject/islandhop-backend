package com.islandhop.userservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TouristRegistrationRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @NotNull
    private LocalDate dateOfBirth;
    
    @NotBlank
    private String nationality;
    
    @NotNull
    private List<String> languages;
} 