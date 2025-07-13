package com.islandhop.tripinit.repository.mongodb;

import com.islandhop.tripinit.model.mongodb.InitiatedTrip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InitiatedTripRepository extends MongoRepository<InitiatedTrip, String> {
    // Additional query methods can be defined here if needed
}