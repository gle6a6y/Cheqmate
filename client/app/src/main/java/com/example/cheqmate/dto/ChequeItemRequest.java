package com.example.cheqmate.dto;

import java.util.List;

public class ChequeItemRequest {
    private String name;
    private double price;
    private int quantity;
    private List<String> participantNames;

    public ChequeItemRequest(String name, double price, int quantity, List<String> participantNames) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.participantNames = participantNames;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public List<String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(List<String> participantNames) { this.participantNames = participantNames; }
}
