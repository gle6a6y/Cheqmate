package project.cheqmate.dto;

import lombok.Data;

@Data
public class UpdateProgressRequest {
    private String player;
    private int moves;
    private boolean finished;
}
