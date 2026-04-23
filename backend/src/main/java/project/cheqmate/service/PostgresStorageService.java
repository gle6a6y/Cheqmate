package project.cheqmate.service;

import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.cheqmate.dto.ChequeItemRequest;
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

    public PostgresStorageService(UserRepository userRepo, GroupRepository groupRepo,
                                  ChequeRepository chequeRepo, DebtRepository debtRepo,
                                  DebtOptimizationService debtOptimizationService,
                                  PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.chequeRepo = chequeRepo;
        this.debtRepo = debtRepo;
        this.debtOptimizationService = debtOptimizationService;
        this.passwordEncoder = passwordEncoder;
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
            summaries.add(new GroupSummaryResponse(
                    group.getId(),
                    group.getGroupName(),
                    group.getMembers().size(),
                    income,
                    expense
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
            double percent = entry.getValue();

            // тот, кто платил, сам себе не должен
            if (userId == whoPaid.getId()) {
                continue;
            }

            double amount = cheque.getTotal() * percent / 100.0;
            User person = userRepo.findById(userId).orElseThrow();

            Debt debt = debtRepo.findByCreditorAndDebtor(whoPaid, person).orElseThrow(() -> new NoSuchElementException("Debt not found"));
            double newAmount = debt.getAmount() - amount;
            if (newAmount <= 1e-6) {
                debtRepo.delete(debt);
            } else {
                debt.setAmount(newAmount);
                debtRepo.save(debt);
            }
        }
        chequeRepo.delete(cheque);
    }

    @Override
    @Transactional
    public Group addUserToGroup(int groupId, String userName) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findByName(userName).orElseThrow();
        group.addMember(user);
        return groupRepo.save(group);
    }

    @Override
    @Transactional
    public Group addUserToGroupByName(String groupName, String userName) {
        Group group = groupRepo.findByGroupName(groupName).orElseThrow(() ->
                new NoSuchElementException("Group not found: " + groupName));
        User user = userRepo.findByName(userName).orElseThrow(() ->
                new NoSuchElementException("User not found: " + userName));
        group.addMember(user);
        return groupRepo.save(group);
    }

    @Override
    @Transactional
    public Cheque createCheque(String groupName, String chequeName, double total, String ownerName, String whoPaidName) {
        Group group = groupRepo.findByGroupName(groupName).orElseThrow();
        User owner = userRepo.findByName(ownerName).orElseThrow();
        User whoPaid = userRepo.findByName(whoPaidName).orElseThrow();
        Cheque cheque = new Cheque(chequeName, total, owner, whoPaid);
        group.addCheque(cheque);
        return chequeRepo.save(cheque);
    }

    @Override
    @Transactional
    public Cheque createCheque(String groupName, String chequeName, double total,
                               String ownerName, String whoPaidName, Map<String, Double> proportions) {
        return createCheque(groupName, chequeName, total, ownerName, whoPaidName, proportions, null);
    }

    @Override
    @Transactional
    public Cheque createCheque(String groupName, String chequeName, double total,
                               String ownerName, String whoPaidName, Map<String, Double> proportions,
                               List<ChequeItemRequest> itemRequests) {

        Group group = groupRepo.findByGroupName(groupName).orElseThrow(() -> new NoSuchElementException("Group not found: " + groupName));
        User owner = userRepo.findByName(ownerName).orElseThrow(() -> new NoSuchElementException("Owner not found: " + ownerName));
        User whoPaid = userRepo.findByName(whoPaidName).orElseThrow(() -> new NoSuchElementException("WhoPaid not found: " + whoPaidName));
        
        Cheque cheque = new Cheque(chequeName, total, owner, whoPaid);

        Map<String, Double> finalProportions = new HashMap<>();
        if (proportions != null) {
            finalProportions.putAll(proportions);
        }

        if (itemRequests != null && !itemRequests.isEmpty()) {
            Map<String, Double> userShares = new HashMap<>();
            for (ChequeItemRequest itemReq : itemRequests) {
                ChequeItem item = new ChequeItem(itemReq.getName(), itemReq.getPrice(), itemReq.getQuantity());
                double itemTotal = itemReq.getPrice() * itemReq.getQuantity();
                
                List<String> participants = itemReq.getParticipantNames();
                if (participants != null && !participants.isEmpty()) {
                    double share = itemTotal / participants.size();
                    for (String pName : participants) {
                        User p = getUserByName(pName);
                        if (p != null) {
                            item.getParticipants().add(p);
                            userShares.put(pName, userShares.getOrDefault(pName, 0.0) + share);
                        }
                    }
                }
                cheque.addItem(item);
            }
            
            // Если пропорции не присланы явно, берем их из позиций
            if (finalProportions.isEmpty()) {
                for (Map.Entry<String, Double> shareEntry : userShares.entrySet()) {
                    finalProportions.put(shareEntry.getKey(), (shareEntry.getValue() / total) * 100.0);
                }
            }
        }

        if (finalProportions.isEmpty()) {
            throw new IllegalArgumentException("No proportions or items provided to calculate debt");
        }
        
        for (Map.Entry<String, Double> entry : finalProportions.entrySet()) {
            User user = getUserByName(entry.getKey());
            if (user != null) {
                cheque.addUser(user.getId(), entry.getValue());
            }
        }

        group.addCheque(cheque);
        chequeRepo.save(cheque);
        applyCheque(cheque.getId());
        return cheque;
    }

    @Override
    @Transactional
    public Cheque playFortuneWheel(String groupName, String chequeName, double total, String ownerName) {
        Group group = groupRepo.findByGroupName(groupName)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new IllegalStateException("Group has no members");
        }
        
        User loser = members.get(new Random().nextInt(members.size()));
        
        Map<String, Double> proportions = Map.of(loser.getName(), 100.0);
        
        return createCheque(groupName, chequeName + " (Wheel Loss: " + loser.getName() + ")", 
                            total, ownerName, loser.getName(), proportions);
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

        for (Map.Entry<Integer, Double> entry : cheque.getProportions().entrySet()) {
            int userId = entry.getKey();
            double percent = entry.getValue();
            if (userId == whoPaid.getId()) continue;

            double amount = cheque.getTotal() * percent / 100.0;
            User person = userRepo.findById(userId).orElseThrow();

            Optional<Debt> existing = debtRepo.findByCreditorAndDebtor(whoPaid, person);
            if (existing.isPresent()) {
                Debt debt = existing.get();
                debt.setAmount(debt.getAmount() + amount);
                debtRepo.save(debt);
            } else {
                debtRepo.save(new Debt(whoPaid, person, cheque.getGroup(), amount));
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
}
