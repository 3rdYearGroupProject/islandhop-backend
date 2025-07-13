package com.islandhop.tripinitiation.model.mongo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DailyPlan {
    private int day;
    private String city;
    private boolean userSelected;
    private List<Place> attractions;
    private List<Place> restaurants;
    private List<Place> hotels;
    private List<String> notes;
}