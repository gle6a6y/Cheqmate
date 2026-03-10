package project.cheqmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cheques")
@Getter
@Setter
@NoArgsConstructor
public class Cheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private double total;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "who_paid_id")
    private User whoPaid;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private Group group;

    @ElementCollection
    @CollectionTable(name = "cheque_proportions", joinColumns = @JoinColumn(name = "cheque_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "percent")
    private Map<Integer, Double> proportions = new HashMap<>();

    public Cheque(String name, double total, User owner, User whoPaid) {
        this.name = name;
        this.total = total;
        this.owner = owner;
        this.whoPaid = whoPaid;
    }

    public void addUser(int userId, double percent) {
        proportions.put(userId, percent);
    }
}
