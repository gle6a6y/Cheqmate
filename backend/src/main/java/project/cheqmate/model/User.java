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

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "members")
    @JsonIgnore
    private List<Group> groups = new ArrayList<>();

    @Transient
    @JsonIgnore
    private LinkedHashMap<String, ArrayList<String>> info = new LinkedHashMap<>();

    public User(String name) {
        this.name = name;
        this.info = new LinkedHashMap<>();
        ArrayList<String> names = new ArrayList<>();
        names.add(name);
        info.put("name", names);
    }
}
