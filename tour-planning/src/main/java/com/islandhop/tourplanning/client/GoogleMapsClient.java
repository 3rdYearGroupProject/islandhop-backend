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
public class GoogleMapsClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Value("${google.maps.api.base-url}")
    private String baseUrl;

    public List<PlaceDTO> searchNearbyPlaces(double latitude, double longitude, int radius, String type) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/place/nearbysearch/json")
                .queryParam("key", apiKey)
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", radius)
                .queryParam("type", type)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return convertToPlaceDTOs(response);
    }

    public PlaceDTO getPlaceDetails(String placeId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/place/details/json")
                .queryParam("key", apiKey)
                .queryParam("place_id", placeId)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return convertToPlaceDTO(response);
    }

    public Map<String, Object> getDirections(String origin, String destination, String mode) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/directions/json")
                .queryParam("key", apiKey)
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .queryParam("mode", mode)
                .build()
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String, Object> getDistanceMatrix(String[] origins, String[] destinations, String mode) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/distancematrix/json")
                .queryParam("key", apiKey)
                .queryParam("origins", String.join("|", origins))
                .queryParam("destinations", String.join("|", destinations))
                .queryParam("mode", mode)
                .build()
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    private List<PlaceDTO> convertToPlaceDTOs(Map<String, Object> response) {
        // Implementation to convert Google Maps response to PlaceDTO list
        // This is a placeholder implementation
        return List.of();
    }

    private PlaceDTO convertToPlaceDTO(Map<String, Object> response) {
        // Implementation to convert Google Maps response to PlaceDTO
        // This is a placeholder implementation
        return new PlaceDTO();
    }
} 