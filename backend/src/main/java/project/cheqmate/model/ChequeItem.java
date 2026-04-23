package project.cheqmate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cheque_items")
@Getter
@Setter
@NoArgsConstructor
public class ChequeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private double price;

    private int quantity = 1;

    @ManyToOne
    @JoinColumn(name = "cheque_id")
    @JsonIgnore
    private Cheque cheque;

    @ManyToMany
    @JoinTable(
            name = "cheque_item_participants",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    public ChequeItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
