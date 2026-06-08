package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameSessionResponse {
    private long ready;
    private List<String> expectedPlayers;
    private List<String> joinedPlayers;
    /** Прогресс каждого участника чека (ходы, финиш, в лобби ли сейчас). */
    private List<PlayerProgressResponse> players;
    /** Кто платит за всех (больше всех ходов). null — игра ещё идёт. */
    private String loser;
}
