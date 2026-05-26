package project.cheqmate.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameSession {
    private long sessionId;
    private List<String> expectedPlayers;
    private List<String> joinedPlayers;
    private String loser;
    public GameSession(long sessionId, List<String> expectedPlayers) {
        this.sessionId = sessionId;
        this.expectedPlayers = expectedPlayers;
        this.joinedPlayers = new ArrayList<>();
    }
}
