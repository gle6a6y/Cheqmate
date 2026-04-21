package project.cheqmate.dto;

import lombok.Data;

@Data
public class FortuneWheelRequest {
    private String groupName;
    private String chequeName;
    private double total;
    private String ownerName;
}
