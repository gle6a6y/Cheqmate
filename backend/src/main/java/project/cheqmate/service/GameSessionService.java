package project.cheqmate.service;

import org.springframework.stereotype.Service;
import project.cheqmate.model.GameSession;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {
    private Map<Long, GameSession> sessions = new ConcurrentHashMap<>();
    private long idLastSession = 100000;

    public long createSession(List<String> players) {
        GameSession session = new GameSession(idLastSession++, players);
        sessions.put(session.getSessionId(), session);
        return session.getSessionId();
    }

    public void join(long sessionId, String player) {
        if (!sessions.containsKey(sessionId)) {
            throw new NoSuchElementException("Session not found" + sessionId);
        }
        sessions.get(sessionId).getJoinedPlayers().add(player);
    }

    public GameSession getSession(long sessionId) {
        if (!sessions.containsKey(sessionId)) {
            throw new NoSuchElementException("Session not found" + sessionId);
        }
        return sessions.get(sessionId);
    }
}
