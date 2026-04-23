package com.example.cheqmate.dto;

import java.util.List;

public class ChequeRequest {
    private String groupName;
    private String chequeName;
    private double total;
    private String ownerName;
    private String whoPaidName;
    private List<ChequeItemRequest> items;

    public ChequeRequest(String groupName, String chequeName, double total, String ownerName, String whoPaidName, List<ChequeItemRequest> items) {
        this.groupName = groupName;
        this.chequeName = chequeName;
        this.total = total;
        this.ownerName = ownerName;
        this.whoPaidName = whoPaidName;
        this.items = items;
    }

    // Getters and Setters
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getChequeName() { return chequeName; }
    public void setChequeName(String chequeName) { this.chequeName = chequeName; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getWhoPaidName() { return whoPaidName; }
    public void setWhoPaidName(String whoPaidName) { this.whoPaidName = whoPaidName; }
    public List<ChequeItemRequest> getItems() { return items; }
    public void setItems(List<ChequeItemRequest> items) { this.items = items; }
}
