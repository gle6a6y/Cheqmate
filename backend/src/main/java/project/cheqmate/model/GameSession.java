package project.cheqmate.model;

import lombok.Data;
import project.cheqmate.dto.GameSessionResponse;
import project.cheqmate.dto.PlayerProgressResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class GameSession {
    private long sessionId;
    private Set<String> expectedPlayers;
    private Set<String> joinedPlayers;
    private final Set<String> readyPlayers = new HashSet<>();
    private final Map<String, GamePlayerState> playerStates = new HashMap<>();
    private String loser;

    public GameSession(long sessionId, Set<String> expectedPlayers) {
        this.sessionId = sessionId;
        this.expectedPlayers = expectedPlayers;
        this.joinedPlayers = new HashSet<>();
        for (String player : expectedPlayers) {
            playerStates.put(player, new GamePlayerState());
        }
    }

    public GameSessionResponse getInfo() {
        List<PlayerProgressResponse> players = new ArrayList<>();
        for (String name : expectedPlayers) {
            GamePlayerState state = playerStates.get(name);
            if (state == null) {
                state = new GamePlayerState();
            }
            players.add(new PlayerProgressResponse(
                    name,
                    state.getMoves(),
                    state.isFinished(),
                    joinedPlayers.contains(name)
            ));
        }
        return new GameSessionResponse(
                readyPlayers.size(),
                expectedPlayers.stream().toList(),
                joinedPlayers.stream().toList(),
                players,
                loser
        );
    }

    /**
     * Когда все в лобби закончили Ханой — проигравший тот, у кого больше ходов (платит за всех).
     */
    public void tryResolveLoser() {
        if (loser != null || joinedPlayers.isEmpty()) {
            return;
        }
        for (String player : joinedPlayers) {
            GamePlayerState state = playerStates.get(player);
            if (state == null || !state.isFinished()) {
                return;
            }
        }

        String worstPlayer = null;
        int maxMoves = -1;
        for (String player : joinedPlayers) {
            int playerMoves = playerStates.get(player).getMoves();
            if (playerMoves > maxMoves) {
                maxMoves = playerMoves;
                worstPlayer = player;
            }
        }
        loser = worstPlayer;
    }

    public long getReady() {
        return readyPlayers.size();
    }

    public boolean markReady(String player) {
        if (!joinedPlayers.contains(player)) {
            return false;
        }
        return readyPlayers.add(player);
    }

    public void disconnect(String player) {
        joinedPlayers.remove(player);
        readyPlayers.remove(player);
    }

    public void updateProgress(String player, int moves, boolean finished) {
        GamePlayerState state = playerStates.get(player);
        if (state == null) {
            state = new GamePlayerState();
            playerStates.put(player, state);
        }
        state.setMoves(moves);
        state.setFinished(finished);
    }
}
