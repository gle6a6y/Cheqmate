package project.cheqmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String password;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    private List<Group> groups = new ArrayList<>();

    @Column(name = "debts_paid_count", columnDefinition = "integer default 0")
    private int debtsPaidCount = 0;

    @Transient
    @JsonIgnore
    private LinkedHashMap<String, ArrayList<String>> info = new LinkedHashMap<>();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.info = new LinkedHashMap<>();
        ArrayList<String> names = new ArrayList<>();
        names.add(name);
        info.put("name", names);
    }
}
