package project.cheqmate.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Entity
@Table(name = "debts")
@Getter
@Setter
@NoArgsConstructor
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

     @ManyToOne
     @JoinColumn(name = "group_id", nullable = false)
     private Group group;

    @ManyToOne
    @JoinColumn(name = "creditor_id", nullable = false)
    private User creditor;

    @ManyToOne
    @JoinColumn(name = "debtor_id", nullable = false)
    private User debtor;

    private double amount;

    public Debt(User creditor, User debtor, Group group, double amount) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.group = group;
        this.amount = amount;
    }
}
