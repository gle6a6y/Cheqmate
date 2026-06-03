package project.game.dto;

import java.util.List;

public class GameSessionResponse {
    private long ready;
    private List<String> expectedPlayers;
    private List<String> joinedPlayers;
    private List<PlayerProgressResponse> players;
    private String loser;

    public long getReady() { return ready; }
    public List<String> getExpectedPlayers() { return expectedPlayers; }
    public List<String> getJoinedPlayers() { return joinedPlayers; }
    public List<PlayerProgressResponse> getPlayers() { return players; }
    public String getLoser() { return loser; }
}
