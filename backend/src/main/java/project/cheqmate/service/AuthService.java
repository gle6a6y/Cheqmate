package project.cheqmate.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.cheqmate.service.JwtService;
import project.cheqmate.dto.LoginRequest;
import project.cheqmate.dto.LoginResponse;
import project.cheqmate.model.User;
import project.cheqmate.service.StorageService;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final StorageService storage;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StorageService storage, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.storage = storage;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = storage.getUserByName(request.getName());
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(user.getName());
        System.out.println(token);
        return new LoginResponse(token, user.getName());
    }
}
