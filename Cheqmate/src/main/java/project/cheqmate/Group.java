package project.cheqmate;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private final String groupName;
    private final ArrayList<User> members;
    private final ArrayList<Cheque> cheques;

    Group(String groupName_) {
        groupName = groupName_;
        members = new ArrayList<>();
        cheques = new ArrayList<>();
    }

    public void addMember(User person) {
        members.add(person);
    }

    public void addCheque(Cheque cheque) {
        cheques.add(cheque);
    }

    public void buildGraph() {}

    public String getGroupName() {
        return groupName;
    }
}
