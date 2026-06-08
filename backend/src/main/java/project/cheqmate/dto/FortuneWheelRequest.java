package project.cheqmate.dto;

import lombok.Data;

@Data
public class FortuneWheelRequest {
    private String groupName;
    private String chequeName;
    private double total;
    private String ownerName;
    
    public FortuneWheelRequest() {}
    
    public FortuneWheelRequest(String groupName, String chequeName, double total, String ownerName) {
        this.groupName = groupName;
        this.chequeName = chequeName;
        this.total = total;
        this.ownerName = ownerName;
    }
}
