package project.cheqmate.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonalExpenseRequest {
    private String category;
    private double amount;
    private String description;
}
