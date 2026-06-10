package project.cheqmate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.dto.ReliabilityResponse;
import project.cheqmate.model.Debt;
import project.cheqmate.model.User;
import project.cheqmate.repository.DebtRepository;
import project.cheqmate.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
public class ReliabilityService {

    private static final int MIN_DEBTS = 3;  // не будем показывать рейтинг если меньше 3 долгов

    private final DebtRepository debtRepo;
    private final UserRepository userRepo;

    public ReliabilityService(DebtRepository debtRepo, UserRepository userRepo) {
        this.debtRepo = debtRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Double recalculateForUser(String username) {
        User user = userRepo.findByName(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        long paid = debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID);
        long open = debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN);
        long total = paid + open;

        Double rating;
        if (total < MIN_DEBTS) {
            rating = null;
        } else {
            rating = (paid * 100.0) / total;
        }

        user.setReliabilityRating(rating);
        userRepo.save(user);

        return rating;
    }

    @Transactional(readOnly = true)
    public ReliabilityResponse getForUser(String username) {
        User user = userRepo.findByName(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        long paid = debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID);
        long open = debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN);

        return new ReliabilityResponse(
                user.getName(),
                user.getReliabilityRating(),
                paid,
                paid + open
        );
    }
}