package project.cheqmate.legacy;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class Cheque {
    private final String name;
    private final double total;
    private final int ownerId;
    private final int whoPaidId;
    private final HashMap<Integer, Double> proportions;

    public Cheque(String name_, double total_, int ownerId_, int whoPaidId_) {
        name = name_;
        total = total_;
        ownerId = ownerId_;
        whoPaidId = whoPaidId_;
        proportions = new HashMap<>();
    }

    public void addUser(int userId, double percent) {
        proportions.put(userId, percent);
    }
}
