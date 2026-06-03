package project.cheqmate.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Group group;

    @ElementCollection
    @CollectionTable(name = "cheque_proportions", joinColumns = @JoinColumn(name = "cheque_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "amount")
    private Map<Integer, Double> proportions = new HashMap<>();

    @OneToMany(mappedBy = "cheque", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChequeItem> items = new ArrayList<>();

    public Cheque(String name, User owner, User whoPaid) {
        this.name = name;
        this.owner = owner;
        this.whoPaid = whoPaid;
    }

    public void addItem(ChequeItem item) {
        items.add(item);
        item.setCheque(this);
    }

    public void calculateCheque() {
        this.total = 0.0;
        this.proportions.clear();

        for (ChequeItem item : items) {
            double itemTotalCost = item.getPrice() * item.getQuantity();
            this.total += itemTotalCost;

            List<User> participants = item.getParticipants();

            if (participants == null || participants.isEmpty()) {
                continue;
            }

            double costPerParticipant = itemTotalCost / participants.size();

            for (User user : participants) {
                double currentDebt = proportions.getOrDefault(user.getId(), 0.0);
                proportions.put(user.getId(), currentDebt + costPerParticipant);
            }
        }
    }

    public void addUser(int userId, double percent) {
        proportions.put(userId, percent);
    }

    public String getChequeName() {
        return this.name;
    }
}