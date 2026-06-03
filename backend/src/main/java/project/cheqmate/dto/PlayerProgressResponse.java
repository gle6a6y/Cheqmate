package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerProgressResponse {
    private String name;
    private int moves;
    private boolean finished;
    private boolean joined;
}
