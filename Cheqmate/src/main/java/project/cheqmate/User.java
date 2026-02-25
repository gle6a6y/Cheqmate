package project.cheqmate;

import java.util.HashMap;

public class User {
    private final String name;
    private final HashMap<User, Double> debtors;
    private final HashMap<User, Double> creditors;

    User(String name_) {
        debtors = new HashMap<>();
        creditors = new HashMap<>();
        name = name_;
    }

    public void addDebtors(User person, double amount) {
        debtors.merge(person, amount, Double::sum);
    }

    public void addCreditors(User person, double amount) {
        creditors.merge(person, amount, Double::sum);
    }

    public String getName() {
        return name;
    }

    public HashMap<User, Double> getDebtors() {
        return debtors;
    }

    public HashMap<User, Double> getCreditors() {
        return creditors;
    }
}
