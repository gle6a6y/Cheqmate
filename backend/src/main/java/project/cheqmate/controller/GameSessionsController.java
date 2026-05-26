package project.cheqmate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateGameSessionRequest;
import project.cheqmate.dto.JoinGameSessionRequest;
import project.cheqmate.model.GameSession;
import project.cheqmate.service.GameSessionService;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/game-sessions")
public class GameSessionsController {
    private GameSessionService gameSessionService;

    public GameSessionsController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping
    public long createSession(@RequestBody CreateGameSessionRequest req) {
        return gameSessionService.createSession(req.getPlayers());
    }

    @PostMapping("/{sessionId}/join")
    public void join(@PathVariable long sessionId, @RequestBody JoinGameSessionRequest req) {
        gameSessionService.join(sessionId, req.getPlayer());
    }

    @GetMapping("/{sessionId}")
    public GameSession getSession(@PathVariable long sessionId) {
        return gameSessionService.getSession(sessionId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
