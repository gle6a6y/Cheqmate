package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.model.User;
import project.cheqmate.service.StorageService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final StorageService storage;

    public UserController(StorageService storage) {
        this.storage = storage;
    }

//    @GetMapping
//    public List<User> getUsers() {
//        return storage.getUsers();
//    }

    @GetMapping("/me/debts")
    public Map<String, List<Map<String, Object>>> getMyDebts(Principal principal) {
        return storage.getDebtsByUsername(principal.getName());
    }

//    @GetMapping("/{id}")
//    public User getUser(@PathVariable int id) {
//        return storage.getUserById(id);
//    }
//
//    @GetMapping("/{id}/debts")
//    public Map<String, List<Map<String, Object>>> getDebts(@PathVariable int id) {
//        return storage.getDebts(id);
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteUser(@PathVariable int id) {
//        storage.deleteUser(id);
//    }
}