package com.islandhop.tripinitiation.dto;

import jakarta.validation.constraints.NotBlank;

public class TripInitiationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Trip ID is required")
    private String tripId;

    private int setDriver; // 1 or 0

    private int setGuide; // 1 or 0

    @NotBlank(message = "Preferred vehicle type ID is required")
    private String preferredVehicleTypeId;

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public int getSetDriver() {
        return setDriver;
    }

    public void setSetDriver(int setDriver) {
        this.setDriver = setDriver;
    }

    public int getSetGuide() {
        return setGuide;
    }

    public void setSetGuide(int setGuide) {
        this.setGuide = setGuide;
    }

    public String getPreferredVehicleTypeId() {
        return preferredVehicleTypeId;
    }

    public void setPreferredVehicleTypeId(String preferredVehicleTypeId) {
        this.preferredVehicleTypeId = preferredVehicleTypeId;
    }
}