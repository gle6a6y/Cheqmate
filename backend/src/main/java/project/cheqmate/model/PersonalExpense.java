package project.cheqmate.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "personal_expenses")
@Getter
@Setter
@NoArgsConstructor
public class PersonalExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double amount;

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    public PersonalExpense(User user, String category, double amount, String description, LocalDate date) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }
}
