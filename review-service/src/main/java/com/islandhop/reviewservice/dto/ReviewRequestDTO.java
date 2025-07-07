package com.islandhop.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; // Driver or Guide email

    @NotBlank(message = "Review content is required")
    private String review;

    @NotBlank(message = "Reviewer email is required")
    @Email(message = "Invalid reviewer email format")
    private String reviewerEmail;

    @NotBlank(message = "Reviewer first name is required")
    private String reviewerFirstname;

    @NotBlank(message = "Reviewer last name is required")
    private String reviewerLastname;
}
