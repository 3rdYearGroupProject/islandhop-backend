package com.islandhop.pooling.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date formatting and trip duration calculations.
 */
public class DateUtils {
    
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("MMM d");
    
    /**
     * Formats date range for display.
     * Examples: "Aug 15-17, 2025", "Aug 15 - Sep 2, 2025"
     * 
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     * @return Formatted date range string
     */
    public static String formatDateRange(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        
        try {
            LocalDate start = LocalDate.parse(startDate, INPUT_FORMAT);
            LocalDate end = LocalDate.parse(endDate, INPUT_FORMAT);
            
            String startFormatted = start.format(OUTPUT_FORMAT);
            String endFormatted = end.format(OUTPUT_FORMAT);
            
            // If same month, show "Aug 15-17, 2025"
            if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
                return startFormatted + "-" + end.getDayOfMonth() + ", " + start.getYear();
            } else {
                // Different months: "Aug 15 - Sep 2, 2025"
                return startFormatted + " - " + endFormatted + ", " + end.getYear();
            }
            
        } catch (Exception e) {
            return startDate + " - " + endDate;
        }
    }
    
    /**
     * Calculates trip duration in days (inclusive).
     * 
     * @param startDate Start date in "yyyy-MM-dd" format
     * @param endDate End date in "yyyy-MM-dd" format
     * @return Number of days
     */
    public static int calculateTripDuration(String startDate, String endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        
        try {
            LocalDate start = LocalDate.parse(startDate, INPUT_FORMAT);
            LocalDate end = LocalDate.parse(endDate, INPUT_FORMAT);
            
            return (int) ChronoUnit.DAYS.between(start, end) + 1; // +1 for inclusive count
            
        } catch (Exception e) {
            return 0;
        }
    }
}
