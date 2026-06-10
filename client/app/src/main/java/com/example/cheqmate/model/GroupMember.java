package com.example.cheqmate.model;

public class GroupMember {

    private final String name;
    private final Double reliabilityRating;
    private final boolean currentUser;

    public GroupMember(String name, Double reliabilityRating, boolean currentUser) {
        this.name = name;
        this.reliabilityRating = reliabilityRating;
        this.currentUser = currentUser;
    }

    public String getName() {
        return name;
    }

    public Double getReliabilityRating() {
        return reliabilityRating;
    }

    public boolean isCurrentUser() {
        return currentUser;
    }
}
