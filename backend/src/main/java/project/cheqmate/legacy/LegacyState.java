package project.cheqmate.legacy;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class LegacyState {
    private int numberOfUsers;
    private HashMap<Integer, User> userById;
    private ArrayList<User> users;
    private ArrayList<Group> groups;

    public LegacyState() {
        numberOfUsers = 0;
        userById = new HashMap<>();
        users = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
        userById.put(user.getId(), user);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public int getNumberOfUsersAndIncrement() {
        int response = numberOfUsers;
        numberOfUsers++;
        return response;
    }

    public User getUserById(int id) {
        return userById.get(id);
    }

    public void createUser(String username) throws IOException {
        User user = new User(this.getNumberOfUsersAndIncrement(), username);
        this.addUser(user);
    }

    public Group createGroup(String groupName) {
        Group group = new Group(groupName);
        this.addGroup(group);
        return group;
    }

    public void addUserToGroup(Group group, User member) {
        group.addMember(member);
    }

    public Cheque createCheque(Group group, String chequeName, double total, int ownerId, int whoPaidId) {
        Cheque cheque = new Cheque(chequeName, total, ownerId, whoPaidId);
        group.addCheque(cheque);
        return cheque;
    }

    public void addUserToCheque(Cheque cheque, User user, double value) {
        cheque.addUser(user.getId(), value);
    }

    public void applyCheque(Cheque cheque) {
        for (Map.Entry<Integer, Double> e : cheque.getProportions().entrySet()) {
            int userId = e.getKey();
            double percent = e.getValue();
            if (userId == cheque.getWhoPaidId()) continue;
            double amount = cheque.getTotal() * percent / 100.0;
            User whoPaid = this.getUserById(cheque.getWhoPaidId());
            User person = this.getUserById(userId);
            whoPaid.addDebtors(person, amount);
            person.addCreditors(whoPaid, amount);
        }
    }
}
