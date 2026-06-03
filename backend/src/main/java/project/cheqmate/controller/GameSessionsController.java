package project.cheqmate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateGameSessionRequest;
import project.cheqmate.dto.GameSessionResponse;
import project.cheqmate.dto.JoinGameSessionRequest;
import project.cheqmate.dto.ReadyRequest;
import project.cheqmate.dto.UpdateProgressRequest;
import project.cheqmate.exception.PlayerAlreadyJoinedException;
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

    @PostMapping("/{sessionId}/disconnect/{player}")
    public void disconnect(@PathVariable long sessionId, @PathVariable String player) {
        gameSessionService.disconnect(sessionId, player);
    }

    @PostMapping("/{sessionId}/ready")
    public long ready(@PathVariable long sessionId, @RequestBody ReadyRequest req) {
        return gameSessionService.ready(sessionId, req.getPlayer());
    }

    @PostMapping("/{sessionId}/progress")
    public void updateProgress(@PathVariable long sessionId, @RequestBody UpdateProgressRequest req) {
        gameSessionService.updateProgress(sessionId, req.getPlayer(), req.getMoves(), req.isFinished());
    }

    @GetMapping("/{sessionId}")
    public GameSessionResponse getSession(@PathVariable long sessionId) {
        return gameSessionService.getSession(sessionId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(PlayerAlreadyJoinedException.class)
    public ResponseEntity<String> handleAlreadyJoined(PlayerAlreadyJoinedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
