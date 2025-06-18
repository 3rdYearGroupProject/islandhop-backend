package com.islandhop.tourplanning.service;

import com.islandhop.tourplanning.dto.TripDTO;
import java.util.List;

public interface TripService {
    TripDTO createTrip(TripDTO tripDTO);
    TripDTO updateTrip(String id, TripDTO tripDTO);
    void deleteTrip(String id);
    TripDTO getTrip(String id);
    List<TripDTO> getUserTrips(String userId);
    List<TripDTO> getPublicTrips();
    TripDTO addPlaceToTrip(String tripId, String placeId);
    TripDTO removePlaceFromTrip(String tripId, String placeId);
    TripDTO updateTripStatus(String tripId, String status);
} 