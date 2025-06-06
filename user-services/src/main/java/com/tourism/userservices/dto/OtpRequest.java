package com.tourism.userservices.dto;

import javax.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "OTP is required")
    private String otp;

    public OtpRequest() {
    }

    public OtpRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}