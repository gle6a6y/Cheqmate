package com.example.cheqmate.dto;

import java.util.List;
import java.util.Map;

public class DebtResponse {
    private List<DebtItem> debtors;
    private List<DebtItem> creditors;

    public List<DebtItem> getDebtors() { return debtors; }
    public List<DebtItem> getCreditors() { return creditors; }

    public static class DebtItem {
        private String name;
        private double amount;

        public String getName() { return name; }
        public double getAmount() { return amount; }
    }
}
