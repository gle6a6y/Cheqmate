package project.game.dto;

public class PlayerProgressResponse {
    private String name;
    private int moves;
    private boolean finished;
    private boolean joined;

    public String getName() { return name; }
    public int getMoves() { return moves; }
    public boolean isFinished() { return finished; }
    public boolean isJoined() { return joined; }
}
