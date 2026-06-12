package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PersonalExpenseResponse {
    private Integer id;
    private String category;
    private double amount;
    private String description;
    private LocalDate date;
}
