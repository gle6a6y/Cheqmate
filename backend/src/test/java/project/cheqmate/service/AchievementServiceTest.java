package project.cheqmate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import project.cheqmate.event.AchievementUnlockedEvent;
import project.cheqmate.model.PersonalExpense;
import project.cheqmate.model.User;
import project.cheqmate.model.UserAchievement;
import project.cheqmate.repository.ChequeRepository;
import project.cheqmate.repository.PersonalExpenseRepository;
import project.cheqmate.repository.UserAchievementRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private UserAchievementRepository achievementRepo;

    @Mock
    private ChequeRepository chequeRepo;

    @Mock
    private PersonalExpenseRepository personalExpenseRepo;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AchievementService achievementService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("bob", "password");
    }

    @Test
    void unlock_savesAchievementAndPublishesEvent_whenNotYetUnlocked() {
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.HONEST)).thenReturn(false);

        achievementService.unlock(user, AchievementService.HONEST);

        verify(achievementRepo).save(any(UserAchievement.class));

        ArgumentCaptor<AchievementUnlockedEvent> captor = ArgumentCaptor.forClass(AchievementUnlockedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("bob");
        assertThat(captor.getValue().achievementKey()).isEqualTo(AchievementService.HONEST);
    }

    @Test
    void unlock_doesNothing_whenAlreadyUnlocked() {
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.HONEST)).thenReturn(true);

        achievementService.unlock(user, AchievementService.HONEST);

        verify(achievementRepo, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void onChequeSaved_unlocksFirstCheque_whenFirstChequeCreated() {
        when(chequeRepo.countByOwner(user)).thenReturn(1L);
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.FIRST_CHEQUE)).thenReturn(false);

        achievementService.onChequeSaved(user, false);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.FIRST_CHEQUE)));
    }

    @Test
    void onChequeSaved_unlocksScanner_whenFromQr() {
        when(chequeRepo.countByOwner(user)).thenReturn(1L);
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.FIRST_CHEQUE)).thenReturn(true);
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.SCANNER)).thenReturn(false);

        achievementService.onChequeSaved(user, true);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.SCANNER)));
    }

    @Test
    void onChequeSaved_doesNotUnlockScanner_whenNotFromQr() {
        when(chequeRepo.countByOwner(user)).thenReturn(1L);
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.FIRST_CHEQUE)).thenReturn(true);

        achievementService.onChequeSaved(user, false);

        verify(achievementRepo, never()).save(argThat(a -> a.getAchievementKey().equals(AchievementService.SCANNER)));
    }

    @Test
    void onPaidForOthers_unlocksGenerous_whenPaidFiveTimes() {
        when(chequeRepo.countByWhoPaid(user)).thenReturn(5L);
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.GENEROUS)).thenReturn(false);

        achievementService.onPaidForOthers(user);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.GENEROUS)));
    }

    @Test
    void onPaidForOthers_doesNotUnlockGenerous_whenPaidFewerThanFiveTimes() {
        when(chequeRepo.countByWhoPaid(user)).thenReturn(4L);

        achievementService.onPaidForOthers(user);

        verify(achievementRepo, never()).save(any());
    }

    @Test
    void onDebtPaid_unlocksHonest() {
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.HONEST)).thenReturn(false);

        achievementService.onDebtPaid(user);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.HONEST)));
    }

    @Test
    void onRouletteLoss_unlocksLoser() {
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.LOSER)).thenReturn(false);

        achievementService.onRouletteLoss(user);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.LOSER)));
    }

    @Test
    void onPersonalExpenseSaved_unlocksBigSpender_whenTotalExceeds10000() {
        PersonalExpense e1 = new PersonalExpense(user, "food", 6000, null, LocalDate.now());
        PersonalExpense e2 = new PersonalExpense(user, "fun", 5000, null, LocalDate.now());
        when(personalExpenseRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(e1, e2));
        when(achievementRepo.existsByUserAndAchievementKey(user, AchievementService.BIG_SPENDER)).thenReturn(false);

        achievementService.onPersonalExpenseSaved(user);

        verify(achievementRepo).save(argThat(a -> a.getAchievementKey().equals(AchievementService.BIG_SPENDER)));
    }

    @Test
    void onPersonalExpenseSaved_doesNotUnlock_whenTotalBelow10000() {
        PersonalExpense e = new PersonalExpense(user, "food", 5000, null, LocalDate.now());
        when(personalExpenseRepo.findByUserOrderByDateDesc(user)).thenReturn(List.of(e));

        achievementService.onPersonalExpenseSaved(user);

        verify(achievementRepo, never()).save(any());
    }

    @Test
    void getAchievements_returnsUserAchievements() {
        UserAchievement achievement = new UserAchievement(user, AchievementService.HONEST);
        when(achievementRepo.findByUser(user)).thenReturn(List.of(achievement));

        List<UserAchievement> result = achievementService.getAchievements(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAchievementKey()).isEqualTo(AchievementService.HONEST);
    }
}
