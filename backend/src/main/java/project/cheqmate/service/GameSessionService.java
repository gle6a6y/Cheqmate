package project.cheqmate.service;

import org.springframework.stereotype.Service;
import project.cheqmate.dto.GameSessionResponse;
import project.cheqmate.exception.PlayerAlreadyJoinedException;
import project.cheqmate.model.GameSession;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private Map<Long, GameSession> sessions = new ConcurrentHashMap<>();
    private long idLastSession = 100000;

    public long createSession(List<String> players) {
        GameSession session = new GameSession(idLastSession++, new HashSet<>(players));
        sessions.put(session.getSessionId(), session);
        return session.getSessionId();
    }

    public void join(long sessionId, String player) {
        if (!sessions.containsKey(sessionId)) {
            throw new NoSuchElementException("Session not found " + sessionId);
        }
        if (!sessions.get(sessionId).getExpectedPlayers().contains(player)) {
            throw new IllegalArgumentException("Player is not in session: " + player);
        }
        if (sessions.get(sessionId).getJoinedPlayers().contains(player)) {
            throw new PlayerAlreadyJoinedException("Player already in session: " + player);
        }
        sessions.get(sessionId).getJoinedPlayers().add(player);
    }

    public void disconnect(long sessionId, String player) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found " + sessionId);
        }
        session.disconnect(player);
    }

    public long ready(long sessionId, String player) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found " + sessionId);
        }
        if (!session.getExpectedPlayers().contains(player)) {
            throw new IllegalArgumentException("Player not in session: " + player);
        }
        session.markReady(player);
        return session.getReady();
    }

    public void updateProgress(long sessionId, String player, int moves, boolean finished) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchElementException("Session not found " + sessionId);
        }
        if (!session.getExpectedPlayers().contains(player)) {
            throw new IllegalArgumentException("Player is not in session: " + player);
        }
        if (!session.getJoinedPlayers().contains(player)) {
            throw new IllegalArgumentException("Player is not in lobby: " + player);
        }
        session.updateProgress(player, moves, finished);
        session.tryResolveLoser();
    }

    public GameSessionResponse getSession(long sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new NoSuchElementException("Session not found" + sessionId);
        }

        return sessions.get(sessionId).getInfo();
    }
}
