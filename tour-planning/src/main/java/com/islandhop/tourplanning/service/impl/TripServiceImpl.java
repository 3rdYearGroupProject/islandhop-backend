package com.islandhop.tourplanning.service.impl;

import com.islandhop.tourplanning.dto.TripDTO;
import com.islandhop.tourplanning.model.Trip;
import com.islandhop.tourplanning.repository.TripRepository;
import com.islandhop.tourplanning.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    @Autowired
    private TripRepository tripRepository;

    @Override
    public TripDTO createTrip(TripDTO tripDTO) {
        Trip trip = convertToEntity(tripDTO);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(tripRepository.save(trip));
    }

    @Override
    public TripDTO updateTrip(String id, TripDTO tripDTO) {
        Trip existingTrip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        
        updateTripFromDTO(existingTrip, tripDTO);
        existingTrip.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(tripRepository.save(existingTrip));
    }

    @Override
    public void deleteTrip(String id) {
        tripRepository.deleteById(id);
    }

    @Override
    public TripDTO getTrip(String id) {
        return tripRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
    }

    @Override
    public List<TripDTO> getUserTrips(String userId) {
        return tripRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getPublicTrips() {
        return tripRepository.findByIsPublicTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TripDTO addPlaceToTrip(String tripId, String placeId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        // Add place to trip logic here
        trip.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(tripRepository.save(trip));
    }

    @Override
    public TripDTO removePlaceFromTrip(String tripId, String placeId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        // Remove place from trip logic here
        trip.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(tripRepository.save(trip));
    }

    @Override
    public TripDTO updateTripStatus(String tripId, String status) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        trip.setStatus(status);
        trip.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(tripRepository.save(trip));
    }

    private Trip convertToEntity(TripDTO dto) {
        Trip trip = new Trip();
        trip.setId(dto.getId());
        trip.setUserId(dto.getUserId());
        trip.setName(dto.getName());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setStatus(dto.getStatus());
        trip.setPublic(dto.isPublic());
        trip.setCreatedAt(dto.getCreatedAt());
        trip.setUpdatedAt(dto.getUpdatedAt());
        return trip;
    }

    private TripDTO convertToDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setUserId(trip.getUserId());
        dto.setName(trip.getName());
        dto.setDescription(trip.getDescription());
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        dto.setStatus(trip.getStatus());
        dto.setPublic(trip.isPublic());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setUpdatedAt(trip.getUpdatedAt());
        return dto;
    }

    private void updateTripFromDTO(Trip trip, TripDTO dto) {
        trip.setName(dto.getName());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setStatus(dto.getStatus());
        trip.setPublic(dto.isPublic());
    }
} 