package project.cheqmate.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import project.cheqmate.legacy.FileStorage;
import project.cheqmate.legacy.LegacyState;
import project.cheqmate.model.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

@Service
@Primary
@ConditionalOnProperty(name = "cheqmate.use-legacy-file-storage", havingValue = "true")
public class LegacyFileStorageService implements StorageService {

    private final LegacyState state = new LegacyState();
    private final FileStorage fileStorage = new FileStorage();

    public LegacyFileStorageService() {
        try {
            fileStorage.loadState(state);
        } catch (IOException ignored) {
        }
    }

    private void persist() {
        try {
            fileStorage.saveState(state);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public User createUser(String name) {
        try {
            state.createUser(name);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        persist();
        var legacy = state.getUsers().get(state.getUsers().size() - 1);
        return toUserFromLegacyUser(legacy);
    }

    @Override
    public List<User> getUsers() {
        List<User> result = new ArrayList<>();
        for (var u : state.getUsers()) {
            result.add(toUserFromLegacyUser(u));
        }
        return result;
    }

    @Override
    public User getUserById(int id) {
        var u = state.getUserById(id);
        return u == null ? null : toUserFromLegacyUser(u);
    }

    @Override
    public User getUserByName(String name) {
        for (var u : state.getUsers()) {
            if (u.getName().equals(name)) return toUserFromLegacyUser(u);
        }
        return null;
    }

    @Override
    public Group createGroup(String groupName) {
        var g = state.createGroup(groupName);
        persist();
        return toGroupFromLegacyGroup(g);
    }

    @Override
    public Group createGroupWithMembers(String groupName, List<String> memberNames) {return null;} // дописать потом если надо будет

    @Override
    public List<Group> getGroups() {
        List<Group> result = new ArrayList<>();
        for (var g : state.getGroups()) {
            result.add(toGroupFromLegacyGroup(g));
        }
        return result;
    }

    public Group getGroupById(int id) { return null; } // дописать потом если надо будет

    @Override
    public Group getGroupByName(String groupName) {
        for (var g : state.getGroups()) {
            if (g.getGroupName().equals(groupName)) return toGroupFromLegacyGroup(g);
        }
        return null;
    }

    @Override
    public Group changeGroupName(int id, String newName) {
        for (var g : state.getGroups()) {
            //if (g.getId().equals(id)) {
                g.setGroupName(newName);
                persist();
                return toGroupFromLegacyGroup(g);
            //}
        }
        return null;
    }

    @Override
    public void deleteGroup(int id) {
        project.cheqmate.legacy.Group groupToDelete = null;
        for (var g : state.getGroups()) {
//            if (g.getId() == id) {
//                groupToDelete = g;
//                break;
//            }
        }

        if (groupToDelete != null) {
            state.getGroups().remove(groupToDelete);
            persist();
        }
    }

    @Override
    public void deleteUser(int id) { // потом допишем
        return;
    }

    public void deleteCheque(int id) { // потом допишем
        return;
    }

//    @Override
//    public Group addUserToGroup(int groupId, int userId) {
//        throw new UnsupportedOperationException("Use addUserToGroupByName for legacy mode");
//    }

    public Group addUserToGroup(int groupId, String userName) {return null;} // потом допишем если надо

    @Override
    public Group addUserToGroupByName(String groupName, String userName) {
        project.cheqmate.legacy.Group lg = findLegacyGroup(groupName);
        project.cheqmate.legacy.User lu = findLegacyUser(userName);
        if (lg != null && lu != null) {
            state.addUserToGroup(lg, lu);
            persist();
            return toGroupFromLegacyGroup(lg);
        }
        return null;
    }

    @Override
    public Cheque createCheque(String groupName, String chequeName, double total,
                               String ownerName, String whoPaidName) {
        project.cheqmate.legacy.Group lg = findLegacyGroup(groupName);
        project.cheqmate.legacy.User owner = findLegacyUser(ownerName);
        project.cheqmate.legacy.User whoPaid = findLegacyUser(whoPaidName);
        if (lg == null || owner == null || whoPaid == null) return null;
        var lc = state.createCheque(lg, chequeName, total, owner.getId(), whoPaid.getId());
        persist();

        Cheque c = new Cheque();
        c.setId(0);
        c.setName(chequeName);
        c.setTotal(total);
        c.setProportions(lc.getProportions());
        return c;
    }

    @Override
    public Cheque createCheque(String groupName, String chequeName, double total, String ownerName, String whoPaidName, Map<String, Double> proportions) {
        return null;
    } // пока не надо

    @Override
    public void addUserToCheque(int chequeId, int userId, double percent) {
        throw new UnsupportedOperationException("Legacy mode uses inline cheque building");
    }

    @Override
    public void applyCheque(int chequeId) {
        throw new UnsupportedOperationException("Legacy mode applies cheques inline");
    }

    @Override
    public Map<String, List<Map<String, Object>>> getDebts(int userId) {
        var lu = state.getUserById(userId);
        if (lu == null) return Map.of();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        List<Map<String, Object>> debtors = new ArrayList<>();
        for (var e : lu.getDebtors().entrySet()) {
            debtors.add(Map.of("name", e.getKey().getName(), "amount", e.getValue()));
        }
        result.put("debtors", debtors);
        List<Map<String, Object>> creditors = new ArrayList<>();
        for (var e : lu.getCreditors().entrySet()) {
            creditors.add(Map.of("name", e.getKey().getName(), "amount", e.getValue()));
        }
        result.put("creditors", creditors);
        return result;
    }

    @Override
    public List<Debt> getAllDebts() {
        return List.of();
    }

    private project.cheqmate.legacy.User findLegacyUser(String name) {
        for (var u : state.getUsers()) {
            if (u.getName().equals(name)) return u;
        }
        return null;
    }

    private project.cheqmate.legacy.Group findLegacyGroup(String name) {
        for (var g : state.getGroups()) {
            if (g.getGroupName().equals(name)) return g;
        }
        return null;
    }

    private User toUserFromLegacyUser(project.cheqmate.legacy.User lu) {
        User u = new User(lu.getName());
        u.setId(lu.getId());
        return u;
    }

    private Group toGroupFromLegacyGroup(project.cheqmate.legacy.Group lg) {
        Group g = new Group(lg.getGroupName());
        List<User> members = new ArrayList<>();
        for (var m : lg.getMembers()) members.add(toUserFromLegacyUser(m));
        g.setMembers(members);
        return g;
    }
}
