package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReliabilityResponse {
    private String username;
    private Double reliabilityRating;  // null если нельзя показать
    private long paidCount;
    private long totalCount;
}