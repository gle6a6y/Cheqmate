package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.CreateUserRequest;
import project.cheqmate.model.User;
import project.cheqmate.service.StorageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final StorageService storage;

    public UserController(StorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    public User createUser(@RequestBody CreateUserRequest req) {
        return storage.createUser(req.getName());
    }

    @GetMapping
    public List<User> getUsers() {
        return storage.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return storage.getUserById(id);
    }

    @GetMapping("/{id}/debts")
    public Map<String, List<Map<String, Object>>> getDebts(@PathVariable int id) {
        return storage.getDebts(id);
    }
}