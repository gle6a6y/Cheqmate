package project.cheqmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups_table")
@Getter
@Setter
@NoArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String groupName;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cheque> cheques = new ArrayList<>();

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public void addMember(User person) {
        members.add(person);
    }

    public void addCheque(Cheque cheque) {
        cheques.add(cheque);
        cheque.setGroup(this);
    }
}
