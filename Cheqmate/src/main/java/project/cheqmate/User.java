package project.cheqmate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class User {
    private final String name;
    private final HashMap<User, Double> debtors;
    private final HashMap<User, Double> creditors;
    private final LinkedHashMap<String, ArrayList<String>> info;

    User(String name_) throws IOException {
        debtors = new HashMap<>();
        creditors = new HashMap<>();
        name = name_;
        info = new LinkedHashMap<>();
        ArrayList<String> names = new ArrayList<>();
        names.add(name);
        info.put("name", names);
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

    public LinkedHashMap<String, ArrayList<String>> getInfo() {
        return info;
    }
}
