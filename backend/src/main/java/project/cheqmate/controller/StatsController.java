package project.cheqmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cheqmate.dto.UserStatsResponse;
import project.cheqmate.model.User;
import project.cheqmate.repository.ChequeRepository;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserRepository;

import java.security.Principal;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepo;
    private final ChequeRepository chequeRepo;
    private final project.cheqmate.repository.GroupRepository groupRepo;

    @GetMapping("/my")
    public UserStatsResponse myStats(Principal principal) {
        User user = userRepo.findByName(principal.getName()).orElseThrow();
        int groupsCount = (int) groupRepo.countByMembersContaining(user);
        long chequesCount = chequeRepo.countByOwner(user);
        return new UserStatsResponse(groupsCount, chequesCount, user.getDebtsPaidCount());
    }
}
