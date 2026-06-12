package project.cheqmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.model.User;
import project.cheqmate.model.UserAchievement;
import project.cheqmate.repository.ChequeRepository;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserAchievementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final UserAchievementRepository achievementRepo;
    private final ChequeRepository chequeRepo;
    private final PersonalExpenseRepository personalExpenseRepo;

    public static final String FIRST_CHEQUE  = "FIRST_CHEQUE";
    public static final String GENEROUS      = "GENEROUS";
    public static final String HONEST        = "HONEST";
    public static final String LOSER         = "LOSER";
    public static final String SCANNER       = "SCANNER";
    public static final String BIG_SPENDER   = "BIG_SPENDER";

    @Transactional
    public void unlock(User user, String key) {
        if (!achievementRepo.existsByUserAndAchievementKey(user, key)) {
            achievementRepo.save(new UserAchievement(user, key));
        }
    }

    @Transactional
    public void onChequeSaved(User owner, boolean fromQr) {
        long count = chequeRepo.countByOwner(owner);
        if (count >= 1) unlock(owner, FIRST_CHEQUE);
        if (fromQr) unlock(owner, SCANNER);
    }

    @Transactional
    public void onPaidForOthers(User payer) {
        long count = chequeRepo.countByWhoPaid(payer);
        if (count >= 5) unlock(payer, GENEROUS);
    }

    @Transactional
    public void onDebtPaid(User debtor) {
        unlock(debtor, HONEST);
    }

    @Transactional
    public void onRouletteLoss(User loser) {
        unlock(loser, LOSER);
    }

    @Transactional
    public void onPersonalExpenseSaved(User user) {
        double total = personalExpenseRepo.findByUserOrderByDateDesc(user)
                .stream().mapToDouble(e -> e.getAmount()).sum();
        if (total >= 10000) unlock(user, BIG_SPENDER);
    }

    public List<UserAchievement> getAchievements(User user) {
        return achievementRepo.findByUser(user);
    }
}
