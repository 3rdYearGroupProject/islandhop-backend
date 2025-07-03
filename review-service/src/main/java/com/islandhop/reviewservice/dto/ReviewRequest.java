package com.islandhop.reviewservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Review text is required")
    @Size(max = 500, message = "Review text should not exceed 500 characters")
    private String review;

    @NotNull(message = "Rating is required")
    private Integer rating;

    public ReviewRequest() {
    }

    public ReviewRequest(String email, String review, Integer rating) {
        this.email = email;
        this.review = review;
        this.rating = rating;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}