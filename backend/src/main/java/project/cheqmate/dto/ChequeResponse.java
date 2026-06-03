package project.cheqmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChequeResponse { // for front
    private Integer id;
    private String chequeName;
    private double total;
    private String ownerName;
    private String whoPaidName;
    private Map<Integer, Double> proportions;
}