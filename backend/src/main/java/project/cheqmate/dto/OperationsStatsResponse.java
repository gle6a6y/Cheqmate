package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperationsStatsResponse {
    private double personalSpent;
    private double paidForOthers;
}
