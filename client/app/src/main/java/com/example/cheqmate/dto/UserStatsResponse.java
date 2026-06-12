package com.example.cheqmate.dto;

public class UserStatsResponse {
    private int groupsCount;
    private long chequesCount;
    private int debtsPaidCount;

    public int getGroupsCount() { return groupsCount; }
    public long getChequesCount() { return chequesCount; }
    public int getDebtsPaidCount() { return debtsPaidCount; }
}
