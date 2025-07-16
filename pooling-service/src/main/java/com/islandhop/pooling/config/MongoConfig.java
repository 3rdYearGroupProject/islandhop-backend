package com.islandhop.pooling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB configuration for the pooling service.
 * Uses MongoDB Atlas cloud connection.
 */
@Configuration
@Slf4j
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.database}")
    private String databaseName;
    
    @Override
    protected String getDatabaseName() {
        log.info("Using MongoDB database: {}", databaseName);
        return databaseName;
    }
    
    @Override
    public MongoClient mongoClient() {
        log.info("Configuring MongoDB client with Atlas URI");
        log.info("MongoDB URI: {}", mongoUri.replaceAll("://[^:]+:[^@]+@", "://***:***@")); // Hide credentials in logs
        
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        
        return MongoClients.create(mongoClientSettings);
    }
    
    /**
     * Create indexes for optimal query performance.
     */
    public void createIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOps = mongoTemplate.indexOps("groups");
        
        // Index on groupId for unique lookups
        indexOps.ensureIndex(new Index().on("groupId", org.springframework.data.domain.Sort.Direction.ASC).unique());
        
        // Index on visibility for public group queries
        indexOps.ensureIndex(new Index().on("visibility", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Index on tripId for trip-based queries
        indexOps.ensureIndex(new Index().on("tripId", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Index on userIds for member queries
        indexOps.ensureIndex(new Index().on("userIds", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Compound index on visibility and preferences.interests for filtered public group queries
        indexOps.ensureIndex(new Index()
                .on("visibility", org.springframework.data.domain.Sort.Direction.ASC)
                .on("preferences.interests", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Index on preferences.destination for destination-based queries
        indexOps.ensureIndex(new Index().on("preferences.destination", org.springframework.data.domain.Sort.Direction.ASC));
        
        // Index on createdAt for time-based queries
        indexOps.ensureIndex(new Index().on("createdAt", org.springframework.data.domain.Sort.Direction.DESC));
    }
}
