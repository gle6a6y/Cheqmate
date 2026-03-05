package project.cheqmate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter
public class User {
    private int id;
    private final String name;
    @JsonIgnore
    private final HashMap<User, Double> debtors;
    @JsonIgnore
    private final HashMap<User, Double> creditors;
    @JsonIgnore
    private final LinkedHashMap<String, ArrayList<String>> info;

    User(int id_, String name_) throws IOException {
        id = id_;
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
}
