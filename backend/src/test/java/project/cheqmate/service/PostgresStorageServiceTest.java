package project.cheqmate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.dto.ChequeItemRequest;
import project.cheqmate.dto.ChequeResponse;
import project.cheqmate.dto.GroupSummaryResponse;
import project.cheqmate.model.Cheque;
import project.cheqmate.model.Group;
import project.cheqmate.model.User;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "cheqmate.kafka.enabled=false")
@Transactional
class PostgresStorageServiceTest {

    @Autowired
    private StorageService storageService;

    @MockitoBean
    private FcmService fcmService;

    // --- createUser ---

    @Test
    void createUser_savesAndReturnsUser() {
        User user = storageService.createUser("alice", "pass");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("alice");
    }

    @Test
    void createUser_throws_whenNameAlreadyExists() {
        storageService.createUser("alice", "pass");

        assertThatThrownBy(() -> storageService.createUser("alice", "other"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alice");
    }

    @Test
    void getUserById_throws_whenNotFound() {
        assertThatThrownBy(() -> storageService.getUserById(999))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- createGroup ---

    @Test
    void createGroup_savesAndReturnsGroup() {
        Group group = storageService.createGroup("Друзья");

        assertThat(group.getId()).isNotNull();
        assertThat(group.getGroupName()).isEqualTo("Друзья");
    }

    @Test
    void changeGroupName_updatesName() {
        Group group = storageService.createGroup("Старое");

        Group updated = storageService.changeGroupName(group.getId(), "Новое");

        assertThat(updated.getGroupName()).isEqualTo("Новое");
    }

    @Test
    @WithMockUser(username = "alice")
    void deleteGroup_removesGroup() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.deleteGroup(group.getId());

        assertThatThrownBy(() -> storageService.getGroupById(group.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- addUserToGroup ---

    @Test
    @WithMockUser(username = "alice")
    void addUserToGroup_addsUserCorrectly() {
        storageService.createUser("alice", "pass");
        Group group = storageService.createGroup("Team");

        Group result = storageService.addUserToGroup(group.getId(), "alice");

        assertThat(result.getMembers()).anyMatch(u -> u.getName().equals("alice"));
    }

    // --- getGroupsByUser ---

    @Test
    @WithMockUser(username = "alice")
    void getGroupsByUser_returnsGroupsWithDebtSummary() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        ChequeItemRequest item = new ChequeItemRequest();
        item.setName("Пицца");
        item.setPrice(500.0);
        item.setQuantity(1);
        item.setParticipantNames(List.of("alice", "bob"));

        storageService.createCheque(group.getId(), "Ужин", "alice", "alice",
                null, List.of(item));

        List<GroupSummaryResponse> groups = storageService.getGroupsByUser("alice");

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getName()).isEqualTo("Team");
        assertThat(groups.get(0).getIncome()).isGreaterThan(0);
    }

    // --- createCheque ---

    @Test
    @WithMockUser(username = "alice")
    void createCheque_withProportions_createsDebts() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        Cheque cheque = storageService.createCheque(group.getId(), "Кофе", "alice", "alice",
                Map.of("bob", 200.0), null);

        assertThat(cheque.getTotal()).isEqualTo(200.0);
        Map<String, List<Map<String, Object>>> debts = storageService.getDebtsByUsernameAndGroup("alice", group.getId());
        assertThat(debts.get("debtors")).anyMatch(d -> d.get("name").equals("bob"));
    }

    @Test
    @WithMockUser(username = "alice")
    void createCheque_withItems_calculatesProportions() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        ChequeItemRequest item = new ChequeItemRequest();
        item.setName("Бургер");
        item.setPrice(300.0);
        item.setQuantity(2);
        item.setParticipantNames(List.of("bob"));

        Cheque cheque = storageService.createCheque(group.getId(), "Обед", "alice", "alice",
                null, List.of(item));

        assertThat(cheque.getTotal()).isEqualTo(600.0);
    }

    @Test
    @WithMockUser(username = "alice")
    void createCheque_throws_whenNoItemsOrProportions() {
        storageService.createUser("alice", "pass");
        storageService.createGroupWithMembers("Solo", List.of("alice"));
        Group group = storageService.getGroupByName("Solo");

        assertThatThrownBy(() ->
                storageService.createCheque(group.getId(), "Пусто", "alice", "alice", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- getChequesByGroupId ---

    @Test
    @WithMockUser(username = "alice")
    void getChequesByGroupId_returnsCreatedCheques() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.createCheque(group.getId(), "Такси", "alice", "alice",
                Map.of("bob", 150.0), null);

        List<ChequeResponse> cheques = storageService.getChequesByGroupId(group.getId());

        assertThat(cheques).hasSize(1);
        assertThat(cheques.get(0).getChequeName()).isEqualTo("Такси");
        assertThat(cheques.get(0).getTotal()).isEqualTo(150.0);
    }

    // --- payDebtInGroup ---

    @Test
    @WithMockUser(username = "alice")
    void payDebt_reducesDebtAmount() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.createCheque(group.getId(), "Кофе", "alice", "alice",
                Map.of("bob", 300.0), null);

        storageService.payDebtInGroup("bob", group.getId(), "alice", 100.0);

        Map<String, List<Map<String, Object>>> debts = storageService.getDebtsByUsernameAndGroup("bob", group.getId());
        double remaining = (double) debts.get("creditors").get(0).get("amount");
        assertThat(remaining).isEqualTo(200.0);
    }

    @Test
    @WithMockUser(username = "alice")
    void payDebt_closesDebt_whenPaidInFull() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.createCheque(group.getId(), "Кофе", "alice", "alice",
                Map.of("bob", 300.0), null);

        storageService.payDebtInGroup("bob", group.getId(), "alice", 300.0);

        Map<String, List<Map<String, Object>>> debts = storageService.getDebtsByUsernameAndGroup("bob", group.getId());
        assertThat(debts.get("creditors")).isEmpty();
    }

    @Test
    @WithMockUser(username = "alice")
    void payDebt_throws_whenAmountExceedsDebt() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.createCheque(group.getId(), "Кофе", "alice", "alice",
                Map.of("bob", 300.0), null);

        assertThatThrownBy(() -> storageService.payDebtInGroup("bob", group.getId(), "alice", 500.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    void payDebt_throws_whenPayingYourself() {
        storageService.createUser("alice", "pass");
        storageService.createGroup("Solo");
        Group group = storageService.getGroupByName("Solo");

        assertThatThrownBy(() -> storageService.payDebtInGroup("alice", group.getId(), "alice", 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void payDebt_throws_whenAmountIsNegative() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");

        assertThatThrownBy(() -> storageService.payDebtInGroup("bob", 999, "alice", -50.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    // --- deleteUser ---

    @Test
    @WithMockUser(username = "alice")
    void deleteUser_removesUserAndTheirDebts() {
        storageService.createUser("alice", "pass");
        storageService.createUser("bob", "pass");
        storageService.createGroupWithMembers("Team", List.of("alice", "bob"));
        Group group = storageService.getGroupByName("Team");

        storageService.createCheque(group.getId(), "Кофе", "alice", "alice",
                Map.of("bob", 100.0), null);

        User bob = storageService.getUserByName("bob");
        storageService.deleteUser(bob.getId());

        assertThat(storageService.getUserByName("bob")).isNull();
    }
}
