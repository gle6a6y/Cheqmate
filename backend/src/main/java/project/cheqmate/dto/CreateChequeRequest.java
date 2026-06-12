package project.cheqmate.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateChequeRequest {
    private int groupId;
    private String chequeName;
    private String ownerName;
    private String whoPaidName;
    private List<ChequeItemRequest> items;
    private boolean fromQr;
    private boolean fromRoulette;
}