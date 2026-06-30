package project.cheqmate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.cheqmate.dto.ReliabilityResponse;
import project.cheqmate.model.Debt;
import project.cheqmate.model.User;
import project.cheqmate.repository.DebtRepository;
import project.cheqmate.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReliabilityServiceTest {

    @Mock
    private DebtRepository debtRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ReliabilityService reliabilityService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("alice", "password");
    }

    @Test
    void recalculate_returnsNull_whenFewerThanThreeDebts() {
        when(userRepo.findByName("alice")).thenReturn(Optional.of(user));
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID)).thenReturn(1L);
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN)).thenReturn(1L);

        Double result = reliabilityService.recalculateForUser("alice");

        assertThat(result).isNull();
        assertThat(user.getReliabilityRating()).isNull();
        verify(userRepo).save(user);
    }

    @Test
    void recalculate_returnsCorrectPercentage_whenEnoughDebts() {
        when(userRepo.findByName("alice")).thenReturn(Optional.of(user));
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID)).thenReturn(3L);
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN)).thenReturn(1L);

        Double result = reliabilityService.recalculateForUser("alice");

        assertThat(result).isEqualTo(75.0);
        assertThat(user.getReliabilityRating()).isEqualTo(75.0);
    }

    @Test
    void recalculate_returns100_whenAllDebtsPaid() {
        when(userRepo.findByName("alice")).thenReturn(Optional.of(user));
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID)).thenReturn(5L);
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN)).thenReturn(0L);

        Double result = reliabilityService.recalculateForUser("alice");

        assertThat(result).isEqualTo(100.0);
    }

    @Test
    void recalculate_returns0_whenNoDebtsPaid() {
        when(userRepo.findByName("alice")).thenReturn(Optional.of(user));
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID)).thenReturn(0L);
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN)).thenReturn(5L);

        Double result = reliabilityService.recalculateForUser("alice");

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void recalculate_throws_whenUserNotFound() {
        when(userRepo.findByName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reliabilityService.recalculateForUser("ghost"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void getForUser_returnsCorrectResponse() {
        when(userRepo.findByName("alice")).thenReturn(Optional.of(user));
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.PAID)).thenReturn(4L);
        when(debtRepo.countByDebtorAndStatus(user, Debt.Status.OPEN)).thenReturn(1L);
        user.setReliabilityRating(80.0);

        ReliabilityResponse response = reliabilityService.getForUser("alice");

        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getReliabilityRating()).isEqualTo(80.0);
        assertThat(response.getPaidCount()).isEqualTo(4L);
        assertThat(response.getTotalCount()).isEqualTo(5L);
    }

    @Test
    void getForUser_throws_whenUserNotFound() {
        when(userRepo.findByName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reliabilityService.getForUser("ghost"))
                .isInstanceOf(NoSuchElementException.class);
    }
}
