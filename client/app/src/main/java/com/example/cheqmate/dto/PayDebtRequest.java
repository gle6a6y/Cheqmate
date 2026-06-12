package com.example.cheqmate.dto;

import lombok.Data;

public class PayDebtRequest {
    private String creditorUsername;
    private String debtorUsername;
    private double amount;

    public void setCreditorUsername(String v) { creditorUsername = v; }
    public void setDebtorUsername(String v) { debtorUsername = v; }
    public void setAmount(double v) { amount = v; }
}
