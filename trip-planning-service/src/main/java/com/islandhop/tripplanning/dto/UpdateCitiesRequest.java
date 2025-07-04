package com.islandhop.tripplanning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCitiesRequest {
    private List<String> cities;
    private Map<String, Integer> cityDays; // city -> number of days
}
