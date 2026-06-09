package project.cheqmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.cheqmate.dto.OperationsStatsResponse;
import project.cheqmate.dto.UserStatsResponse;
import project.cheqmate.model.Cheque;
import project.cheqmate.model.PersonalExpense;
import project.cheqmate.model.User;
import project.cheqmate.repository.ChequeRepository;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepo;
    private final ChequeRepository chequeRepo;
    private final PersonalExpenseRepository personalExpenseRepo;
    private final project.cheqmate.repository.GroupRepository groupRepo;

    @GetMapping("/my")
    public UserStatsResponse myStats(Principal principal) {
        User user = userRepo.findByName(principal.getName()).orElseThrow();
        int groupsCount = (int) groupRepo.countByMembersContaining(user);
        long chequesCount = chequeRepo.countByOwner(user);
        return new UserStatsResponse(groupsCount, chequesCount, user.getDebtsPaidCount());
    }

    @GetMapping("/my/operations")
    @Transactional(readOnly = true)
    public OperationsStatsResponse myOperations(Principal principal) {
        User user = userRepo.findByName(principal.getName()).orElseThrow();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();
        Instant from = monthStart.atStartOfDay(zone).toInstant();
        Instant to = monthEnd.plusDays(1).atStartOfDay(zone).toInstant();

        double personalSpent = personalExpenseRepo
                .findByUserAndDateBetweenOrderByDateDesc(user, monthStart, monthEnd)
                .stream()
                .mapToDouble(PersonalExpense::getAmount)
                .sum();

        int payerId = user.getId();
        double paidForOthers = 0;
        for (Cheque cheque : chequeRepo.findByWhoPaid(user)) {
            Instant createdAt = cheque.getCreatedAt();
            if (createdAt != null && (createdAt.isBefore(from) || !createdAt.isBefore(to))) {
                continue;
            }
            Map<Integer, Double> proportions = cheque.getProportions();
            if (proportions == null) {
                continue;
            }
            for (Map.Entry<Integer, Double> entry : proportions.entrySet()) {
                if (!entry.getKey().equals(payerId)) {
                    paidForOthers += entry.getValue();
                }
            }
        }

        return new OperationsStatsResponse(personalSpent, paidForOthers);
    }
}
