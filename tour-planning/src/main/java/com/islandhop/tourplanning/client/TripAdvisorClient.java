package com.islandhop.tourplanning.client;

import com.islandhop.tourplanning.dto.PlaceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class TripAdvisorClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tripadvisor.api.key}")
    private String apiKey;

    @Value("${tripadvisor.api.base-url}")
    private String baseUrl;

    public List<PlaceDTO> searchPlaces(String location, String type) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/locations/search")
                .queryParam("key", apiKey)
                .queryParam("searchQuery", location)
                .queryParam("category", type)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return convertToPlaceDTOs(response);
    }

    public PlaceDTO getPlaceDetails(String placeId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/locations/" + placeId)
                .queryParam("key", apiKey)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return convertToPlaceDTO(response);
    }

    public List<PlaceDTO> getPopularPlaces(String location) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/locations/popular")
                .queryParam("key", apiKey)
                .queryParam("location", location)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return convertToPlaceDTOs(response);
    }

    private List<PlaceDTO> convertToPlaceDTOs(Map<String, Object> response) {
        // Implementation to convert TripAdvisor response to PlaceDTO list
        // This is a placeholder implementation
        return List.of();
    }

    private PlaceDTO convertToPlaceDTO(Map<String, Object> response) {
        // Implementation to convert TripAdvisor response to PlaceDTO
        // This is a placeholder implementation
        return new PlaceDTO();
    }
} 