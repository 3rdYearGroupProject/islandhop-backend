package com.islandhop.pooling.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Data
public class SaveTripRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Trip ID is required")
    private String tripId;
    
    @NotNull(message = "Trip data is required")
    private TripData tripData;
    
    @Data
    public static class TripData {
        private String name;
        private String startDate;
        private String endDate;
        
        @JsonDeserialize(using = DestinationListDeserializer.class)
        private List<Destination> destinations;
        
        private List<String> terrains;
        private List<String> activities;
        private Map<String, DayItinerary> itinerary;
        
        @Data
        public static class Destination {
            private String name;
            
            public Destination() {}
            
            public Destination(String name) {
                this.name = name;
            }
        }
        
        @Data
        public static class DayItinerary {
            private String date;
            private List<Object> activities;
            private List<Object> places;
            private List<Object> food;
            private List<Object> transportation;
        }
    }
    
    /**
     * Custom deserializer to handle both string arrays and object arrays for destinations.
     * Frontend can send either ["Colombo", "Galle"] or [{"name": "Colombo"}, {"name": "Galle"}]
     */
    public static class DestinationListDeserializer extends JsonDeserializer<List<SaveTripRequest.TripData.Destination>> {
        @Override
        public List<SaveTripRequest.TripData.Destination> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            List<SaveTripRequest.TripData.Destination> destinations = new ArrayList<>();
            
            if (node.isArray()) {
                for (JsonNode item : node) {
                    if (item.isTextual()) {
                        // Handle string format: "Colombo"
                        destinations.add(new SaveTripRequest.TripData.Destination(item.asText()));
                    } else if (item.isObject() && item.has("name")) {
                        // Handle object format: {"name": "Colombo"}
                        destinations.add(new SaveTripRequest.TripData.Destination(item.get("name").asText()));
                    }
                }
            }
            
            return destinations;
        }
    }
}