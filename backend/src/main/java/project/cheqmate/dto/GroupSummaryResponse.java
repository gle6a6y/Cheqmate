package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupSummaryResponse {
    private Integer id;
    private String name;
    private int participantsCount;
    private double income;  // сколько мне должны
    private double expense; // сколько я должен
}
