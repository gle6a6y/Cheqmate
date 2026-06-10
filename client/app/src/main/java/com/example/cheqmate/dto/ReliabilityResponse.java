package com.example.cheqmate.dto;

import lombok.Data;

@Data
public class ReliabilityResponse {
    private String username;
    private Double reliabilityRating;
    private long paidCount;
    private long totalCount;
}
