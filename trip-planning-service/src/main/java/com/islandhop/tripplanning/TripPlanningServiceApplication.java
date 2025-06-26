package com.islandhop.tripplanning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableMongoAuditing
@EnableWebFlux
public class TripPlanningServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlanningServiceApplication.class, args);
	}

}
