package project.cheqmate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.PayDebtRequest;
import project.cheqmate.dto.ReliabilityResponse;
import project.cheqmate.service.ReliabilityService;
import project.cheqmate.service.StorageService;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final StorageService storage;
    private final ReliabilityService reliabilityService;

    public UserController(StorageService storage, ReliabilityService reliabilityService) {
        this.storage = storage;
        this.reliabilityService = reliabilityService;
    }

//    @GetMapping
//    public List<User> getUsers() {
//        return storage.getUsers();
//    }

    @GetMapping("/me/debts")
    public Map<String, List<Map<String, Object>>> getMyDebts(Principal principal) {
        return storage.getDebtsByUsername(principal.getName());
    }

    @GetMapping("/me/groups/{groupId}/debts")
    public Map<String, List<Map<String, Object>>> getMyDebtsByGroup(
            Principal principal,
            @PathVariable int groupId
    ) {
        return storage.getDebtsByUsernameAndGroup(principal.getName(), groupId);
    }

    @PostMapping("/me/groups/{groupId}/debts/pay")
    public Map<String, List<Map<String, Object>>> payDebtInGroup(
            Principal principal,
            @PathVariable int groupId,
            @RequestBody PayDebtRequest request
    ) {
        String debtor = request.getDebtorUsername() != null && !request.getDebtorUsername().isBlank()
                ? request.getDebtorUsername()
                : principal.getName();
        return storage.payDebtInGroup(
                debtor,
                groupId,
                request.getCreditorUsername(),
                request.getAmount());
    }

    @GetMapping("/{username}/reliability")
    public ReliabilityResponse getReliability(@PathVariable String username) {
        return reliabilityService.getForUser(username);
    }
    @PostMapping("/{username}/reliability/recalculate")
    public ReliabilityResponse recalculate(@PathVariable String username) {
        reliabilityService.recalculateForUser(username);
        return reliabilityService.getForUser(username);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException e) {
        return Map.of("error", e.getMessage());
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