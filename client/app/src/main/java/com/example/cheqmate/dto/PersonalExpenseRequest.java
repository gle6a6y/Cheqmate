package com.example.cheqmate.dto;

public class PersonalExpenseRequest {
    private String category;
    private double amount;
    private String description;

    public PersonalExpenseRequest(String category, double amount, String description) {
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
}
