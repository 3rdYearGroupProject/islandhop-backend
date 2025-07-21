package com.islandhop.pooling.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CompatibleGroupResponse {
    private String groupId;
    private String tripName;
    private String groupName;
    private double compatibilityScore;
    private int currentMembers;
    private int maxMembers;
    private String createdBy;
    private String startDate;
    private String endDate;
    private List<Map<String, String>> destinations;
    private List<String> activities;
    private List<String> terrains;
}