package project.cheqmate.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cheqmate.dto.CreateUserRequest;
import project.cheqmate.dto.LoginRequest;
import project.cheqmate.dto.LoginResponse;
import project.cheqmate.model.User;
import project.cheqmate.service.AuthService;
import project.cheqmate.service.StorageService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final StorageService storage;

    public AuthController(AuthService authService, StorageService storage) {
        this.authService = authService;
        this.storage = storage;
    }

    @PostMapping("/register")
    public User register(@RequestBody CreateUserRequest req) {
        return storage.createUser(req.getName(), req.getPassword());
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
