package com.example.cheqmate.dto;

import lombok.Data;

@Data
public class PayDebtRequest {
    private String creditorUsername;
    private double amount;
}
