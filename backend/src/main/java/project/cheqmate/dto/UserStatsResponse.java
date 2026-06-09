package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatsResponse {
    private int groupsCount;
    private long chequesCount;
    private int debtsPaidCount;
}
