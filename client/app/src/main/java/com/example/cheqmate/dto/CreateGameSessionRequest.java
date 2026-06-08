package com.example.cheqmate.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateGameSessionRequest {
    private List<String> players;
}
