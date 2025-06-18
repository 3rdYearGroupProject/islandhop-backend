package com.islandhop.tourplanning.repository;

import com.islandhop.tourplanning.model.Place;
import com.islandhop.tourplanning.model.PlaceType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceRepository extends MongoRepository<Place, String> {
    List<Place> findByType(PlaceType type);
    List<Place> findByTripAdvisorId(String tripAdvisorId);
    List<Place> findByGooglePlaceId(String googlePlaceId);
    List<Place> findByTypeAndRatingGreaterThanEqual(PlaceType type, double minRating);
} 