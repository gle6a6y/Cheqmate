package project.cheqmate.service;

import project.cheqmate.model.*;

import java.util.List;
import java.util.Map;

public interface StorageService {

    User createUser(String name);

    List<User> getUsers();

    User getUserById(int id);

    User getUserByName(String name);

    Group createGroup(String groupName);

    List<Group> getGroups();

    Group getGroupByName(String groupName);
    Group changeGroupName(int id, String newName);
    void deleteGroup(int id);

    Group addUserToGroup(int groupId, int userId);

    Group addUserToGroupByName(String groupName, String userName);

    Cheque createCheque(String groupName, String chequeName, double total,
                        String ownerName, String whoPaidName);

    void addUserToCheque(int chequeId, int userId, double percent);

    void applyCheque(int chequeId);

    Map<String, List<Map<String, Object>>> getDebts(int userId);

    List<Debt> getAllDebts();
}
