package com.example.cheqmate.dto;

public class FortuneWheelRequest {
    private String groupName;
    private String chequeName;
    private double total;
    private String ownerName;

    public FortuneWheelRequest(String groupName, String chequeName, double total, String ownerName) {
        this.groupName = groupName;
        this.chequeName = chequeName;
        this.total = total;
        this.ownerName = ownerName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getChequeName() {
        return chequeName;
    }

    public void setChequeName(String chequeName) {
        this.chequeName = chequeName;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}