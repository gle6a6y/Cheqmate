package project.cheqmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.event.AchievementUnlockedEvent;
import project.cheqmate.model.User;
import project.cheqmate.model.UserAchievement;
import project.cheqmate.repository.ChequeRepository;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserAchievementRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final UserAchievementRepository achievementRepo;
    private final ChequeRepository chequeRepo;
    private final PersonalExpenseRepository personalExpenseRepo;
    private final ApplicationEventPublisher eventPublisher;

    public static final String FIRST_CHEQUE  = "FIRST_CHEQUE";
    public static final String GENEROUS      = "GENEROUS";
    public static final String HONEST        = "HONEST";
    public static final String LOSER         = "LOSER";
    public static final String SCANNER       = "SCANNER";
    public static final String BIG_SPENDER   = "BIG_SPENDER";

    public static final Map<String, String[]> META = Map.of(
            FIRST_CHEQUE, new String[]{"🧾 Первый чек", "Создал первый чек"},
            GENEROUS,     new String[]{"🤑 Щедрый", "Оплатил за других 5 раз"},
            HONEST,       new String[]{"😇 Честный", "Погасил долг"},
            LOSER,        new String[]{"📉 Неудачник", "Проиграл рулетку"},
            SCANNER,      new String[]{"📷 Сканер", "Создал чек через QR"},
            BIG_SPENDER,  new String[]{"💰 Транжира", "Личные расходы превысили 10 000 ₽"}
    );

    @Transactional
    public void unlock(User user, String key) {
        if (!achievementRepo.existsByUserAndAchievementKey(user, key)) {
            achievementRepo.save(new UserAchievement(user, key));

            String name = META.getOrDefault(key, new String[]{key, ""})[0];
            eventPublisher.publishEvent(new AchievementUnlockedEvent(user.getName(), key, name));
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
