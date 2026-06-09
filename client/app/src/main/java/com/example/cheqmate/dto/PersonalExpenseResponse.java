package com.example.cheqmate.dto;

public class PersonalExpenseResponse {
    private int id;
    private String category;
    private double amount;
    private String description;
    private String date;

    public int getId() { return id; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
}
