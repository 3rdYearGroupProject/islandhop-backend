package com.islandhop.pooling.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTOs for User Service Integration

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TouristProfileDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String nationality;
    private List<String> languages;
}
