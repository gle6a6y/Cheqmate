package project.cheqmate.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CreateChequeRequest {
    private String groupName;
    private String chequeName;
    private double total;
    private String ownerName;
    private String whoPaidName;
    private Map<String, Double> proportions;
}
