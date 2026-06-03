package project.cheqmate.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateChequeRequest {
    private String groupName;
    private String chequeName;
    private String ownerName;
    private String whoPaidName;
    private List<ChequeItemRequest> items;
}