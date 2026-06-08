package com.example.cheqmate.dto;

import lombok.Data;

@Data
public class GameSessionResponse {
    private long ready;
    private String loser;
}
