package com.tourism.userservices.dto;

import javax.validation.constraints.NotBlank;

public class OtpVerifyRequest {

    @NotBlank(message = "OTP must not be blank")
    private String otp;

    public OtpVerifyRequest() {
    }

    public OtpVerifyRequest(String otp) {
        this.otp = otp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}