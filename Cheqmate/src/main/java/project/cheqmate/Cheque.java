package project.cheqmate;

import java.util.HashMap;

public class Cheque {
    private final String name;
    private final double total;
    private final User owner;
    private final User whoPaid;
    private final HashMap<User, Double> proportions;
    User[] Users; // хз надо ли

    Cheque(String name_, double total_, User owner_, User whoPaid_) {
        name = name_;
        total = total_;
        owner = owner_;
        whoPaid = whoPaid_;
        proportions = new HashMap<>();
    }

    public void addUser(User person, double percent) {
        if (person == whoPaid) {
            return;
        }
        double amount = (double) (total * percent) / 100;
        whoPaid.addDebtors(person, amount);
        person.addCreditors(whoPaid, amount);
        proportions.put(person, percent);
    }

}
