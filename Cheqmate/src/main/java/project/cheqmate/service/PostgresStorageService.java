package project.cheqmate.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public PostgresStorageService(UserRepository userRepo, GroupRepository groupRepo,
                                  ChequeRepository chequeRepo, DebtRepository debtRepo) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.chequeRepo = chequeRepo;
        this.debtRepo = debtRepo;
    }

    @Override
    public User createUser(String name) {
        User user = new User(name);
        return userRepo.save(user);
    }

    @Override
    public List<User> getUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserById(int id) {
        return userRepo.findById(id).orElseThrow(() ->
                new NoSuchElementException("User not found: " + id));
    }

    @Override
    public User getUserByName(String name) {
        return userRepo.findByName(name).orElse(null);
    }

    @Override
    public Group createGroup(String groupName) {
        Group group = new Group(groupName);
        return groupRepo.save(group);
    }

    @Override
    public List<Group> getGroups() {
        return groupRepo.findAll();
    }

    @Override
    public Group getGroupByName(String groupName) {
        return groupRepo.findByGroupName(groupName).orElse(null);
    }

    @Override
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
        groupRepo.delete(group);
    }

    @Override
    @Transactional
    public Group addUserToGroup(int groupId, int userId) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();
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
    public Cheque createCheque(String groupName, String chequeName, double total,
                               String ownerName, String whoPaidName) {
        Group group = groupRepo.findByGroupName(groupName).orElseThrow();
        User owner = userRepo.findByName(ownerName).orElseThrow();
        User whoPaid = userRepo.findByName(whoPaidName).orElseThrow();
        Cheque cheque = new Cheque(chequeName, total, owner, whoPaid);
        group.addCheque(cheque);
        return chequeRepo.save(cheque);
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
                debtRepo.save(new Debt(whoPaid, person, amount));
            }
        }
    }

    @Override
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
    public List<Debt> getAllDebts() {
        return debtRepo.findAll();
    }
}