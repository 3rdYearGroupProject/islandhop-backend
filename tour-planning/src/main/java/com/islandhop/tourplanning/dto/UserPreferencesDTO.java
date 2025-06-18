package com.islandhop.tourplanning.dto;

import java.util.List;

public class UserPreferencesDTO {
    private String id;
    private String userId;
    private List<String> preferredPlaceTypes;
    private double maxBudget;
    private String preferredCurrency;
    private int preferredTripDuration;
    private boolean preferPopularPlaces;
    private boolean preferLessCrowded;
    private List<String> dietaryRestrictions;
    private List<String> accessibilityNeeds;
    private String preferredLanguage;
    private String preferredTransportationMode;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPreferredPlaceTypes() {
        return preferredPlaceTypes;
    }

    public void setPreferredPlaceTypes(List<String> preferredPlaceTypes) {
        this.preferredPlaceTypes = preferredPlaceTypes;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public int getPreferredTripDuration() {
        return preferredTripDuration;
    }

    public void setPreferredTripDuration(int preferredTripDuration) {
        this.preferredTripDuration = preferredTripDuration;
    }

    public boolean isPreferPopularPlaces() {
        return preferPopularPlaces;
    }

    public void setPreferPopularPlaces(boolean preferPopularPlaces) {
        this.preferPopularPlaces = preferPopularPlaces;
    }

    public boolean isPreferLessCrowded() {
        return preferLessCrowded;
    }

    public void setPreferLessCrowded(boolean preferLessCrowded) {
        this.preferLessCrowded = preferLessCrowded;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public List<String> getAccessibilityNeeds() {
        return accessibilityNeeds;
    }

    public void setAccessibilityNeeds(List<String> accessibilityNeeds) {
        this.accessibilityNeeds = accessibilityNeeds;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getPreferredTransportationMode() {
        return preferredTransportationMode;
    }

    public void setPreferredTransportationMode(String preferredTransportationMode) {
        this.preferredTransportationMode = preferredTransportationMode;
    }
} 