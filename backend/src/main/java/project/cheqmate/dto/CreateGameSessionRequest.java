package project.cheqmate.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGameSessionRequest {
    List<String> players;
}
