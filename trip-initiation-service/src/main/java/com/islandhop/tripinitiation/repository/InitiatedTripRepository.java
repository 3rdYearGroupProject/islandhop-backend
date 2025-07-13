package com.islandhop.tripinitiation.repository;

import com.islandhop.tripinitiation.model.mongo.InitiatedTrip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InitiatedTripRepository extends MongoRepository<InitiatedTrip, String> {
}