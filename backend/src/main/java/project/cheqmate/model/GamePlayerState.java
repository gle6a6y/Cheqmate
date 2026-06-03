package project.cheqmate.model;

import lombok.Data;

@Data
public class GamePlayerState {
    private int moves;
    private boolean finished;

    public GamePlayerState() {
        this.moves = 0;
        this.finished = false;
    }
}
