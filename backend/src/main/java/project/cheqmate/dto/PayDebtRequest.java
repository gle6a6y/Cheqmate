package project.cheqmate.dto;

import lombok.Data;

@Data
public class PayDebtRequest {
    private String creditorUsername;
    private String debtorUsername;
    private double amount;
}
