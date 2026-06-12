package com.example.cheqmate.dto;

import java.util.List;

public class ChequeRequest {
    private int groupId;
    private String chequeName;
    private String ownerName;
    private String whoPaidName;
    private List<ChequeItemRequest> items;
    private boolean fromRoulette;

    public ChequeRequest(int groupId, String chequeName, String ownerName, String whoPaidName, List<ChequeItemRequest> items) {
        this.groupId = groupId;
        this.chequeName = chequeName;
        this.ownerName = ownerName;
        this.whoPaidName = whoPaidName;
        this.items = items;
    }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }
    public String getChequeName() { return chequeName; }
    public void setChequeName(String chequeName) { this.chequeName = chequeName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getWhoPaidName() { return whoPaidName; }
    public void setWhoPaidName(String whoPaidName) { this.whoPaidName = whoPaidName; }
    public List<ChequeItemRequest> getItems() { return items; }
    public void setItems(List<ChequeItemRequest> items) { this.items = items; }
    public boolean isFromRoulette() { return fromRoulette; }
    public void setFromRoulette(boolean fromRoulette) { this.fromRoulette = fromRoulette; }
}
