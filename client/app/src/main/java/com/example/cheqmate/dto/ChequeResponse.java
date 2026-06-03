package com.example.cheqmate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChequeResponse {
    private Integer id;
    private String chequeName;
    private double total;
    private String ownerName;
    private String whoPaidName;
}
