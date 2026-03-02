package project.cheqmate;

import java.util.ArrayList;
import java.util.List;

public class State {
    private List<User> users; // можно сделать мапу <имя чела, User>, чтоб быстро по имени можно было найти
    private List<Group> groups; // аналогично

    State() {
        users = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
