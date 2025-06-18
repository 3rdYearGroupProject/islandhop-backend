package com.islandhop.tourplanning.controller;

import com.islandhop.tourplanning.dto.TripDTO;
import com.islandhop.tourplanning.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping
    public ResponseEntity<TripDTO> createTrip(@RequestBody TripDTO tripDTO) {
        return ResponseEntity.ok(tripService.createTrip(tripDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripDTO> updateTrip(@PathVariable String id, @RequestBody TripDTO tripDTO) {
        return ResponseEntity.ok(tripService.updateTrip(id, tripDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable String id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDTO> getTrip(@PathVariable String id) {
        return ResponseEntity.ok(tripService.getTrip(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TripDTO>> getUserTrips(@PathVariable String userId) {
        return ResponseEntity.ok(tripService.getUserTrips(userId));
    }

    @GetMapping("/public")
    public ResponseEntity<List<TripDTO>> getPublicTrips() {
        return ResponseEntity.ok(tripService.getPublicTrips());
    }

    @PostMapping("/{tripId}/places/{placeId}")
    public ResponseEntity<TripDTO> addPlaceToTrip(@PathVariable String tripId, @PathVariable String placeId) {
        return ResponseEntity.ok(tripService.addPlaceToTrip(tripId, placeId));
    }

    @DeleteMapping("/{tripId}/places/{placeId}")
    public ResponseEntity<TripDTO> removePlaceFromTrip(@PathVariable String tripId, @PathVariable String placeId) {
        return ResponseEntity.ok(tripService.removePlaceFromTrip(tripId, placeId));
    }

    @PatchMapping("/{tripId}/status")
    public ResponseEntity<TripDTO> updateTripStatus(@PathVariable String tripId, @RequestParam String status) {
        return ResponseEntity.ok(tripService.updateTripStatus(tripId, status));
    }
} 