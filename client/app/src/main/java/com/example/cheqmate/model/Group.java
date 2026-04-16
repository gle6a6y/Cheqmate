package com.example.cheqmate.model;

public class Group { // содержит те данные которые отобр на экране
    private final int id;
    private final String name;
    private final String lastActivityDate;
    private final int participantsCount;
    private final double income;
    private final double expense;
    private final String icon;

    public Group(int id, String name, String lastActivityDate, int participantsCount,
                 double income, double expense, String icon) {
        this.id = id;
        this.name = name;
        this.lastActivityDate = lastActivityDate;
        this.participantsCount = participantsCount;
        this.income = income;
        this.expense = expense;
        this.icon = icon;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLastActivityDate() { return lastActivityDate; }
    public int getParticipantsCount() { return participantsCount; }
    public double getIncome() { return income; }
    public double getExpense() { return expense; }
    public String getIcon() { return icon; }
}