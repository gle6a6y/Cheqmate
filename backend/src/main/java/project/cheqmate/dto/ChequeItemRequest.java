package project.cheqmate.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChequeItemRequest {
    private String name;
    private double price;
    private int quantity;
    private List<String> participantNames;
}
