package project.cheqmate.service;

import project.cheqmate.dto.ChequeItemRequest;
import project.cheqmate.dto.ChequeResponse;
import project.cheqmate.dto.GroupSummaryResponse;
import project.cheqmate.model.*;

import java.util.List;
import java.util.Map;

public interface StorageService {

    User createUser(String name, String password);

    List<User> getUsers();

    User getUserById(int id);

    User getUserByName(String name);

    Group createGroup(String groupName);

    void createGroupWithMembers(String groupName, List<String> memberNames);

    List<Group> getGroups();
    List<GroupSummaryResponse> getGroupsByUser(String userName);
    Group getGroupById(int id);
    Group getGroupByName(String groupName);
    Group changeGroupName(int id, String newName);
    void deleteGroup(int id);

    void deleteUser(int id);

    void deleteCheque(int id);
    // Group addUserToGroup(int groupId, int userId);

    Group addUserToGroup(int groupId, String userName);

    Group addUserToGroupByName(String groupName, String userName);


    Cheque createCheque(String groupName, String chequeName,
                        String ownerName, String whoPaidName, Map<String, Double> proportions,
                        List<ChequeItemRequest> items);

    Cheque playFortuneWheel(String groupName, String chequeName, double total, String ownerName);
    void addUserToCheque(int chequeId, int userId, double percent);

    void applyCheque(int chequeId);

    Map<String, List<Map<String, Object>>> getDebts(int userId);
    Map<String, List<Map<String, Object>>> getDebtsByUsername(String username);

    List<Debt> getAllDebts();

    List<ChequeResponse> getChequesByGroupId(int groupId);
}
