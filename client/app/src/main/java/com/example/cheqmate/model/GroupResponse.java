package com.example.cheqmate.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("memberNames")
    private List<String> memberNames;

    @SerializedName("participantsCount")
    private int participantsCount;

    @SerializedName("userIncome")
    private double userIncome;

    @SerializedName("userExpense")
    private double userExpense;

    @SerializedName("lastActivityDate")
    private String lastActivityDate;

    @SerializedName("icon")
    private String icon;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }

    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) {
        this.participantsCount = participantsCount;
    }

    public double getUserIncome() { return userIncome; }
    public void setUserIncome(double userIncome) { this.userIncome = userIncome; }

    public double getUserExpense() { return userExpense; }
    public void setUserExpense(double userExpense) { this.userExpense = userExpense; }

    public String getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(String lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}