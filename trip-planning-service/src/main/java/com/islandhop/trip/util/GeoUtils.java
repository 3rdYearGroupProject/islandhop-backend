package com.islandhop.trip.util;

/**
 * Utility class for geographical calculations.
 * Provides methods for distance calculations and other geo-related operations.
 */
public class GeoUtils {
    
    private static final int EARTH_RADIUS = 6371; // Earth's radius in kilometers

    /**
     * Calculate distance between two points using Haversine formula.
     * This method provides accurate distance calculations for geographical coordinates.
     *
     * @param lat1 Latitude of first point in degrees
     * @param lon1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lon2 Longitude of second point in degrees
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    /**
     * Validates if latitude is within valid range.
     *
     * @param latitude The latitude to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validates if longitude is within valid range.
     *
     * @param longitude The longitude to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Validates if both latitude and longitude are valid.
     *
     * @param latitude The latitude to validate
     * @param longitude The longitude to validate
     * @return true if both are valid, false otherwise
     */
    public static boolean areValidCoordinates(double latitude, double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }
}
