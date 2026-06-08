package project.cheqmate.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.dto.ChequeItemRequest;
import project.cheqmate.dto.ChequeResponse;
import project.cheqmate.dto.GroupSummaryResponse;
import project.cheqmate.model.*;
import project.cheqmate.repository.*;

import java.util.*;

@Service
@Primary
public class PostgresStorageService implements StorageService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final ChequeRepository chequeRepo;
    private final DebtRepository debtRepo;
    private final DebtOptimizationService debtOptimizationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public PostgresStorageService(UserRepository userRepo, GroupRepository groupRepo,
                                  ChequeRepository chequeRepo, DebtRepository debtRepo,
                                  DebtOptimizationService debtOptimizationService,
                                  PasswordEncoder passwordEncoder,
                                  ApplicationEventPublisher eventPublisher) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.chequeRepo = chequeRepo;
        this.debtRepo = debtRepo;
        this.debtOptimizationService = debtOptimizationService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public User createUser(String name, String password) {
        if (userRepo.findByName(name).isPresent()) {
            throw new IllegalArgumentException("User with name '" + name + "' already exists");
        }
        User user = new User(name, passwordEncoder.encode(password));
        return userRepo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(int id) {
        return userRepo.findById(id).orElseThrow(() ->
                new NoSuchElementException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByName(String name) {
        return userRepo.findByName(name).orElse(null);
    }

    @Override
    @Transactional
    public Group createGroup(String groupName) {
        Group group = new Group(groupName);
        return groupRepo.save(group);
    }

    @Override
    @Transactional
    public void createGroupWithMembers(String groupName, List<String> memberNames) {
        Group group = new Group(groupName);
        for (String name : memberNames) {
            User user = getUserByName(name);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + name);
            }
            group.addMember(user);
        }
        groupRepo.save(group);

        String currentPrincipalName = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        for (String name : memberNames) {
            if (!name.equals(currentPrincipalName)) {
                eventPublisher.publishEvent(new project.cheqmate.event.UserAddedToGroupEvent(
                        name,
                        group.getGroupName(),
                        currentPrincipalName
                ));
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return groupRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> getGroupsByUser(String userName) {
        User user = getUserByName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }
        List<Group> groups = groupRepo.findByMembersContaining(user);
        List<GroupSummaryResponse> summaries = new ArrayList<>();

        for (Group group : groups) {
            double income = 0;
            double expense = 0;
            List<Debt> debts = debtRepo.findByGroupId(group.getId());
            for (Debt debt : debts) {
                if (debt.getCreditor().getId().equals(user.getId())) {
                    income += debt.getAmount();
                } else if (debt.getDebtor().getId().equals(user.getId())) {
                    expense += debt.getAmount();
                }
            }

            List<String> participants = new ArrayList<>();
            for(User u : group.getMembers()) {
                participants.add(u.getName());
            }

            summaries.add(new GroupSummaryResponse(
                    group.getId(),
                    group.getGroupName(),
                    group.getMembers().size(),
                    income,
                    expense,
                    participants
            ));
        }
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroupById(int id) {
        return groupRepo.findById(id).orElseThrow(() ->
                new NoSuchElementException("Group not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroupByName(String groupName) {
        return groupRepo.findByGroupName(groupName).orElse(null);
    }

    @Override
    @Transactional
    public Group changeGroupName(int id, String newName) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        group.setGroupName(newName);
        return groupRepo.save(group);
    }

    @Override
    @Transactional
    public void deleteGroup(int id) {
        Group group = groupRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + id));

        List<Integer> chequeIds = group.getCheques().stream().map(Cheque::getId).toList();

        for (Integer chequeId : chequeIds) {
            deleteCheque(chequeId);
        }

        groupRepo.delete(group);
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        User user = userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        List<Debt> asCreditor = debtRepo.findByCreditor(user);
        debtRepo.deleteAll(asCreditor);
        List<Debt> asDebtor = debtRepo.findByDebtor(user);
        debtRepo.deleteAll(asDebtor);

        List<Group> groups = groupRepo.findByMembersContaining(user);
        for (Group g : groups) {
            g.getMembers().remove(user);
        }
        groupRepo.saveAll(groups);

        userRepo.delete(user);
    }

    @Override
    @Transactional
    public void deleteCheque(int chequeId) {
        Cheque cheque = chequeRepo.findById(chequeId).orElseThrow();
        User whoPaid = cheque.getWhoPaid();

        for (Map.Entry<Integer, Double> entry : cheque.getProportions().entrySet()) {
            int userId = entry.getKey();
            double amount = entry.getValue();

            if (userId == whoPaid.getId()) {
                continue;
            }

            User person = userRepo.findById(userId).orElseThrow();
            Group group = cheque.getGroup();

            Debt debt = debtRepo.findByCreditorAndDebtorAndGroup(whoPaid, person, group)
                    .orElseThrow(() -> new NoSuchElementException("Debt not found"));

            double newAmount = debt.getAmount() - amount;
            if (newAmount <= 1e-6) {
                debtRepo.delete(debt);
            } else {
                debt.setAmount(newAmount);
                debtRepo.save(debt);
            }
        }

        chequeRepo.delete(cheque);

        debtOptimizationService.optimize(cheque.getGroup());
    }

    @Override
    @Transactional
    public Group addUserToGroup(int groupId, String userName) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findByName(userName).orElseThrow();
        group.addMember(user);
        Group savedGroup = groupRepo.save(group);

        String currentPrincipalName = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        eventPublisher.publishEvent(new project.cheqmate.event.UserAddedToGroupEvent(
                user.getName(),
                group.getGroupName(),
                currentPrincipalName
        ));

        return savedGroup;
    }

    @Override
    @Transactional
    public Group addUserToGroupByName(String groupName, String userName) {
        Group group = groupRepo.findByGroupName(groupName).orElseThrow(() ->
                new NoSuchElementException("Group not found: " + groupName));
        User user = userRepo.findByName(userName).orElseThrow(() ->
                new NoSuchElementException("User not found: " + userName));
        group.addMember(user);
        Group savedGroup = groupRepo.save(group);

        String currentPrincipalName = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        eventPublisher.publishEvent(new project.cheqmate.event.UserAddedToGroupEvent(
                user.getName(),
                group.getGroupName(),
                currentPrincipalName
        ));

        return savedGroup;
    }

    @Override
    @Transactional
    public Cheque createCheque(String groupName, String chequeName,
                               String ownerName, String whoPaidName, Map<String, Double> proportions,
                               List<ChequeItemRequest> itemRequests) {

        Group group = groupRepo.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));
        User owner = userRepo.findByName(ownerName)
                .orElseThrow(() -> new NoSuchElementException("Owner not found: " + ownerName));
        User whoPaid = userRepo.findByName(whoPaidName)
                .orElseThrow(() -> new NoSuchElementException("WhoPaid not found: " + whoPaidName));

        Cheque cheque = new Cheque(chequeName, owner, whoPaid);

        if (itemRequests != null && !itemRequests.isEmpty()) {
            for (ChequeItemRequest itemReq : itemRequests) {
                ChequeItem item = new ChequeItem(itemReq.getName(), itemReq.getPrice(), itemReq.getQuantity());

                List<String> participants = itemReq.getParticipantNames();
                if (participants != null && !participants.isEmpty()) {
                    for (String pName : participants) {
                        User p = getUserByName(pName);
                        if (p != null) {
                            item.getParticipants().add(p);
                        }
                    }
                }
                cheque.addItem(item);
            }

            cheque.calculateCheque();

        } else if (proportions != null && !proportions.isEmpty()) {
            for (Map.Entry<String, Double> entry : proportions.entrySet()) {
                User user = getUserByName(entry.getKey());
                if (user != null) {
                    cheque.addUser(user.getId(), entry.getValue());
                }
            }
            double calculatedTotal = proportions.values().stream().mapToDouble(Double::doubleValue).sum();
            cheque.setTotal(calculatedTotal);
        } else {
            throw new IllegalArgumentException("No items or proportions provided to calculate debt");
        }

        group.addCheque(cheque);
        chequeRepo.save(cheque);

        applyCheque(cheque.getId());

        List<String> groupMemberNames = group.getMembers().stream()
                .map(User::getName)
                .toList();

        eventPublisher.publishEvent(new project.cheqmate.event.ChequeAddedEvent(
                groupMemberNames,
                cheque.getChequeName(),
                group.getGroupName(),
                whoPaidName
        ));

        return cheque;
    }


    @Override
    @Transactional
    public Cheque playFortuneWheel(String groupName, String chequeName, double total, String ownerName) {
        Group group = groupRepo.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));

        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalStateException("Group has no members");
        }

        User loser = members.get(new Random().nextInt(members.size()));

        Map<String, Double> proportions = Map.of(loser.getName(), total);

        return createCheque(groupName, chequeName + " (Wheel Loss: " + loser.getName() + ")",
                ownerName, loser.getName(), proportions, null);
    }

    @Override
    @Transactional
    public void addUserToCheque(int chequeId, int userId, double percent) {
        Cheque cheque = chequeRepo.findById(chequeId).orElseThrow();
        cheque.addUser(userId, percent);
        chequeRepo.save(cheque);
    }

    @Override
    @Transactional
    public void applyCheque(int chequeId) {
        Cheque cheque = chequeRepo.findById(chequeId).orElseThrow();
        User whoPaid = cheque.getWhoPaid();

        Group group = cheque.getGroup();
        for (Map.Entry<Integer, Double> entry : cheque.getProportions().entrySet()) {
            int userId = entry.getKey();
            double amount = entry.getValue();
            if (userId == whoPaid.getId()) {
                continue;
            }
            if (amount <= 1e-6) {
                continue;
            }

            double amount = percent;
            User person = userRepo.findById(userId).orElseThrow();

            Optional<Debt> existing = debtRepo.findByCreditorAndDebtorAndGroup(whoPaid, person, group);
            if (existing.isPresent()) {
                Debt debt = existing.get();
                debt.setAmount(debt.getAmount() + amount);
                debtRepo.save(debt);
            } else {
                debtRepo.save(new Debt(whoPaid, person, group, amount));
            }
        }
        debtOptimizationService.optimize(cheque.getGroup());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getDebtsByUsername(String username) {
        User user = userRepo.findByName(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        return getDebts(user.getId());
    }

    @Override
    @Transactional
    public Map<String, List<Map<String, Object>>> payDebtInGroup(
            String debtorUsername, int groupId, String creditorUsername, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (creditorUsername == null || creditorUsername.isBlank()) {
            throw new IllegalArgumentException("Creditor username is required");
        }

        User debtor = userRepo.findByName(debtorUsername)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + debtorUsername));
        User creditor = userRepo.findByName(creditorUsername)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + creditorUsername));
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupId));

        if (debtor.getId().equals(creditor.getId())) {
            throw new IllegalArgumentException("Cannot pay yourself");
        }

        Debt debt = debtRepo.findByCreditorAndDebtorAndGroup(creditor, debtor, group)
                .orElseThrow(() -> new NoSuchElementException(
                        "Debt not found for creditor " + creditorUsername + " in group " + groupId));

        if (amount > debt.getAmount() + 1e-6) {
            throw new IllegalArgumentException("Amount exceeds debt");
        }

        double newAmount = debt.getAmount() - amount;
        if (newAmount <= 1e-6) {
            debtRepo.delete(debt);
        } else {
            debt.setAmount(newAmount);
            debtRepo.save(debt);
        }

        return getDebtsByUsernameAndGroup(debtorUsername, groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getDebtsByUsernameAndGroup(String username, int groupId) {
        User user = userRepo.findByName(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        List<Map<String, Object>> debtors = new ArrayList<>();
        List<Map<String, Object>> creditors = new ArrayList<>();

        for (Debt d : debtRepo.findByGroupId(groupId)) {
            if (d.getCreditor().getId().equals(user.getId())) {
                debtors.add(Map.of("name", d.getDebtor().getName(), "amount", d.getAmount()));
            } else if (d.getDebtor().getId().equals(user.getId())) {
                creditors.add(Map.of("name", d.getCreditor().getName(), "amount", d.getAmount()));
            }
        }

        result.put("debtors", debtors);
        result.put("creditors", creditors);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getDebts(int userId) {
        User user = userRepo.findById(userId).orElseThrow();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        List<Map<String, Object>> debtors = new ArrayList<>();
        for (Debt d : debtRepo.findByCreditor(user)) {
            debtors.add(Map.of("name", d.getDebtor().getName(), "amount", d.getAmount()));
        }
        result.put("debtors", debtors);

        List<Map<String, Object>> creditors = new ArrayList<>();
        for (Debt d : debtRepo.findByDebtor(user)) {
            creditors.add(Map.of("name", d.getCreditor().getName(), "amount", d.getAmount()));
        }
        result.put("creditors", creditors);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Debt> getAllDebts() {
        return debtRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChequeResponse> getChequesByGroupId(int groupId) {
        Group group = groupRepo.findByIdWithCheques(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found: " + groupId));

        List<ChequeResponse> response = new ArrayList<>();

        for (Cheque c : group.getCheques()) {
            String ownerName = c.getOwner() != null ? c.getOwner().getName() : "";
            String whoPaidName = c.getWhoPaid() != null ? c.getWhoPaid().getName() : "";
            response.add(new ChequeResponse(
                    c.getId(),
                    c.getChequeName(),
                    c.getTotal(),
                    ownerName,
                    whoPaidName,
                    new java.util.HashMap<>(c.getProportions())
            ));
        }

        return response;
    }
}
