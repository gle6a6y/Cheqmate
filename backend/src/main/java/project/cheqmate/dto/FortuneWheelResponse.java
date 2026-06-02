package project.cheqmate.dto;

import lombok.Data;

@Data
public class FortuneWheelResponse {
    private String loser;
    private String message;
    
    public FortuneWheelResponse() {}
    
    public FortuneWheelResponse(String loser) {
        this.loser = loser;
        this.message = "Рулетка выбрала: " + loser;
    }
}