package project.cheqmate.dto;

import lombok.Data;

@Data
public class FortuneWheelRequest {
    private int groupId;
    private String chequeName;
    private double total;
    private String ownerName;
}
