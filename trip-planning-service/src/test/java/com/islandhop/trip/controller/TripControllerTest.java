package com.islandhop.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.islandhop.trip.dto.CreateTripRequest;
import com.islandhop.trip.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TripController.
 * Tests the REST endpoint behavior and validation.
 */
@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTrip_ShouldReturnBadRequest_WhenRequiredFieldsMissing() throws Exception {
        CreateTripRequest request = new CreateTripRequest();
        // Missing required fields

        mockMvc.perform(post("/itinerary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void createTrip_ShouldReturnBadRequest_WhenInvalidDateFormat() throws Exception {
        CreateTripRequest request = new CreateTripRequest();
        request.setUserId("user_123");
        request.setTripName("Test Trip");
        request.setStartDate("invalid-date");
        request.setEndDate("2025-08-15");
        request.setBaseCity("Colombo");

        mockMvc.perform(post("/itinerary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrip_ShouldAcceptValidRequest() throws Exception {
        CreateTripRequest request = new CreateTripRequest();
        request.setUserId("user_789");
        request.setTripName("Sri Lanka Adventure");
        request.setStartDate("2025-08-10");
        request.setEndDate("2025-08-15");
        request.setArrivalTime("21:30");
        request.setBaseCity("Colombo");
        request.setMultiCityAllowed(true);
        request.setActivityPacing("Normal");
        request.setBudgetLevel("Medium");
        request.setPreferredTerrains(List.of("Beach", "Mountain"));
        request.setPreferredActivities(List.of("Hiking", "Cultural Tours"));

        mockMvc.perform(post("/itinerary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
