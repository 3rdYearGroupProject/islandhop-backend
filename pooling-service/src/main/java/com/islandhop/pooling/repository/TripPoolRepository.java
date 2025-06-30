package com.islandhop.pooling.repository;

import com.islandhop.pooling.model.TripPool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripPoolRepository extends MongoRepository<TripPool, String> {
    
    /**
     * Find pools by creator
     */
    List<TripPool> findByCreatedByUserId(String userId);
    
    /**
     * Find pools where user is a member
     */
    @Query("{ 'members.userId': ?0 }")
    List<TripPool> findByMemberUserId(String userId);
    
    /**
     * Find active pools by base city
     */
    List<TripPool> findByBaseCityAndStatus(String baseCity, TripPool.PoolStatus status);
    
    /**
     * Find pools with overlapping dates
     */
    @Query("{ $and: [ " +
           "{ $or: [ " +
           "  { $and: [ { 'startDate': { $lte: ?1 } }, { 'endDate': { $gte: ?0 } } ] }, " +
           "  { $and: [ { 'startDate': { $lte: ?0 } }, { 'endDate': { $gte: ?1 } } ] } " +
           "] }, " +
           "{ 'status': { $in: ['FORMING', 'ACTIVE'] } } " +
           "] }")
    List<TripPool> findPoolsWithDateOverlap(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find public pools that allow join requests
     */
    List<TripPool> findByIsPublicTrueAndAllowJoinRequestsTrueAndStatus(TripPool.PoolStatus status);
    
    /**
     * Find pool by join code
     */
    TripPool findByJoinCodeAndStatus(String joinCode, TripPool.PoolStatus status);
    
    /**
     * Find pools by common cities
     */
    @Query("{ 'commonCities': { $in: ?0 }, 'status': { $in: ['FORMING', 'ACTIVE'] } }")
    List<TripPool> findByCommonCitiesIn(List<String> cities);
    
    /**
     * Find pools by pool type and status
     */
    List<TripPool> findByPoolTypeAndStatus(TripPool.PoolType poolType, TripPool.PoolStatus status);
}
