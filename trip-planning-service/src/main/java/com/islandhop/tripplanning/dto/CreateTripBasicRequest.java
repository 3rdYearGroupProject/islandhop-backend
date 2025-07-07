package com.islandhop.tripplanning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripBasicRequest {
    private String userId;      // Accept userId from frontend
    private String tripName;
    private String startDate;
    private String endDate;
}
