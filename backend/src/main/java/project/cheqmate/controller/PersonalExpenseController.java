package project.cheqmate.controller;

import org.springframework.web.bind.annotation.*;
import project.cheqmate.dto.PersonalExpenseRequest;
import project.cheqmate.dto.PersonalExpenseResponse;
import project.cheqmate.model.PersonalExpense;
import project.cheqmate.model.User;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserRepository;
import project.cheqmate.service.AchievementService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/personal-expenses")
public class PersonalExpenseController {

    private final PersonalExpenseRepository expenseRepo;
    private final UserRepository userRepo;
    private final AchievementService achievementService;

    public PersonalExpenseController(PersonalExpenseRepository expenseRepo, UserRepository userRepo,
                                     AchievementService achievementService) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
        this.achievementService = achievementService;
    }

    @PostMapping
    public PersonalExpenseResponse create(@RequestBody PersonalExpenseRequest req, Principal principal) {
        User user = userRepo.findByName(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        PersonalExpense expense = new PersonalExpense(
                user, req.getCategory(), req.getAmount(), req.getDescription(), LocalDate.now()
        );
        expenseRepo.save(expense);
        achievementService.onPersonalExpenseSaved(user);
        return toResponse(expense);
    }

    @GetMapping
    public List<PersonalExpenseResponse> getAll(Principal principal) {
        User user = userRepo.findByName(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return expenseRepo.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, Principal principal) {
        PersonalExpense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Expense not found"));
        if (!expense.getUser().getName().equals(principal.getName())) {
            throw new IllegalArgumentException("Access denied");
        }
        expenseRepo.delete(expense);
    }

    private PersonalExpenseResponse toResponse(PersonalExpense e) {
        return new PersonalExpenseResponse(e.getId(), e.getCategory(), e.getAmount(), e.getDescription(), e.getDate());
    }
}
