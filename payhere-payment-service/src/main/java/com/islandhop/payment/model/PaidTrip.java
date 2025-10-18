package com.islandhop.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Paid trip entity for MongoDB storage in payed_trips_advance collection
 */
@Document(collection = "payed_trips_advance")
public class PaidTrip {
    
    @Id
    private String id; // This will be the same as the original trip ID
    private String userId;
    private String tripName;
    private String startDate;
    private String endDate;
    private String arrivalTime;
    private String baseCity;
    private Boolean multiCityAllowed;
    private String activityPacing;
    private String budgetLevel;
    private List<String> preferredTerrains;
    private List<String> preferredActivities;
    private List<DailyPlan> dailyPlans;
    private List<MapData> mapData;
    private Instant createdAt;
    private Instant lastUpdated;
    private Integer driverNeeded;
    private Integer guideNeeded;
    private Double averageTripDistance;
    private Integer averageDriverCost;
    private Integer averageGuideCost;
    private String vehicleType;
    
    // New fields for paid trips
    @Field("driver_status")
    private String driverStatus;
    @Field("driver_email")
    private String driverEmail;
    @Field("guide_status")
    private String guideStatus;
    @Field("guide_email")
    private String guideEmail;
    private BigDecimal payedAmount;
    
    // Constructors
    public PaidTrip() {}
    
    // Static nested classes for embedded documents
    public static class DailyPlan {
        private Integer day;
        private String city;
        private Boolean userSelected;
        private List<Place> attractions;
        private List<Place> restaurants;
        private List<Place> hotels;
        private List<String> notes;
        
        // Getters and Setters
        public Integer getDay() { return day; }
        public void setDay(Integer day) { this.day = day; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public Boolean getUserSelected() { return userSelected; }
        public void setUserSelected(Boolean userSelected) { this.userSelected = userSelected; }
        
        public List<Place> getAttractions() { return attractions; }
        public void setAttractions(List<Place> attractions) { this.attractions = attractions; }
        
        public List<Place> getRestaurants() { return restaurants; }
        public void setRestaurants(List<Place> restaurants) { this.restaurants = restaurants; }
        
        public List<Place> getHotels() { return hotels; }
        public void setHotels(List<Place> hotels) { this.hotels = hotels; }
        
        public List<String> getNotes() { return notes; }
        public void setNotes(List<String> notes) { this.notes = notes; }
    }
    
    public static class Place {
        private String name;
        private String type;
        private List<String> terrainTags;
        private List<String> activityTags;
        private Location location;
        private Double distanceFromCenterKm;
        private Integer visitDurationMinutes;
        private String popularityLevel;
        private Double rating;
        private String thumbnailUrl;
        private String source;
        private String placeId;
        private String googlePlaceId;
        private Boolean userSelected;
        private List<String> warnings;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public List<String> getTerrainTags() { return terrainTags; }
        public void setTerrainTags(List<String> terrainTags) { this.terrainTags = terrainTags; }
        
        public List<String> getActivityTags() { return activityTags; }
        public void setActivityTags(List<String> activityTags) { this.activityTags = activityTags; }
        
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
        
        public Double getDistanceFromCenterKm() { return distanceFromCenterKm; }
        public void setDistanceFromCenterKm(Double distanceFromCenterKm) { this.distanceFromCenterKm = distanceFromCenterKm; }
        
        public Integer getVisitDurationMinutes() { return visitDurationMinutes; }
        public void setVisitDurationMinutes(Integer visitDurationMinutes) { this.visitDurationMinutes = visitDurationMinutes; }
        
        public String getPopularityLevel() { return popularityLevel; }
        public void setPopularityLevel(String popularityLevel) { this.popularityLevel = popularityLevel; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public String getThumbnailUrl() { return thumbnailUrl; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getPlaceId() { return placeId; }
        public void setPlaceId(String placeId) { this.placeId = placeId; }
        
        public String getGooglePlaceId() { return googlePlaceId; }
        public void setGooglePlaceId(String googlePlaceId) { this.googlePlaceId = googlePlaceId; }
        
        public Boolean getUserSelected() { return userSelected; }
        public void setUserSelected(Boolean userSelected) { this.userSelected = userSelected; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
    
    public static class Location {
        private Double lat;
        private Double lng;
        
        // Getters and Setters
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }
    
    public static class MapData {
        private String label;
        private Double lat;
        private Double lng;
        
        // Getters and Setters
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        
        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }
    }
    
    // Main class getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTripName() { return tripName; }
    public void setTripName(String tripName) { this.tripName = tripName; }
    
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    
    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public String getBaseCity() { return baseCity; }
    public void setBaseCity(String baseCity) { this.baseCity = baseCity; }
    
    public Boolean getMultiCityAllowed() { return multiCityAllowed; }
    public void setMultiCityAllowed(Boolean multiCityAllowed) { this.multiCityAllowed = multiCityAllowed; }
    
    public String getActivityPacing() { return activityPacing; }
    public void setActivityPacing(String activityPacing) { this.activityPacing = activityPacing; }
    
    public String getBudgetLevel() { return budgetLevel; }
    public void setBudgetLevel(String budgetLevel) { this.budgetLevel = budgetLevel; }
    
    public List<String> getPreferredTerrains() { return preferredTerrains; }
    public void setPreferredTerrains(List<String> preferredTerrains) { this.preferredTerrains = preferredTerrains; }
    
    public List<String> getPreferredActivities() { return preferredActivities; }
    public void setPreferredActivities(List<String> preferredActivities) { this.preferredActivities = preferredActivities; }
    
    public List<DailyPlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<DailyPlan> dailyPlans) { this.dailyPlans = dailyPlans; }
    
    public List<MapData> getMapData() { return mapData; }
    public void setMapData(List<MapData> mapData) { this.mapData = mapData; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Integer getDriverNeeded() { return driverNeeded; }
    public void setDriverNeeded(Integer driverNeeded) { this.driverNeeded = driverNeeded; }
    
    public Integer getGuideNeeded() { return guideNeeded; }
    public void setGuideNeeded(Integer guideNeeded) { this.guideNeeded = guideNeeded; }
    
    public Double getAverageTripDistance() { return averageTripDistance; }
    public void setAverageTripDistance(Double averageTripDistance) { this.averageTripDistance = averageTripDistance; }
    
    public Integer getAverageDriverCost() { return averageDriverCost; }
    public void setAverageDriverCost(Integer averageDriverCost) { this.averageDriverCost = averageDriverCost; }
    
    public Integer getAverageGuideCost() { return averageGuideCost; }
    public void setAverageGuideCost(Integer averageGuideCost) { this.averageGuideCost = averageGuideCost; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    // New fields getters and setters
    public String getDriverStatus() { return driverStatus; }
    public void setDriverStatus(String driverStatus) { this.driverStatus = driverStatus; }
    
    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    
    public String getGuideStatus() { return guideStatus; }
    public void setGuideStatus(String guideStatus) { this.guideStatus = guideStatus; }
    
    public String getGuideEmail() { return guideEmail; }
    public void setGuideEmail(String guideEmail) { this.guideEmail = guideEmail; }
    
    public BigDecimal getPayedAmount() { return payedAmount; }
    public void setPayedAmount(BigDecimal payedAmount) { this.payedAmount = payedAmount; }
    
    @Override
    public String toString() {
        return "PaidTrip{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", tripName='" + tripName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", payedAmount=" + payedAmount +
                '}';
    }
}
