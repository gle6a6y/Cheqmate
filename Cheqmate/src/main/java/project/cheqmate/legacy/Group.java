package project.cheqmate.legacy;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class Group {
    private final String groupName;
    private final ArrayList<User> members;
    private final ArrayList<Cheque> cheques;

    public Group(String groupName_) {
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
}
