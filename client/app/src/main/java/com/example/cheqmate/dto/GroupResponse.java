package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupResponse {
    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("participantsCount")
    private int participantsCount;

    @SerializedName("income")
    private double income;

    @SerializedName("expense")
    private double expense;

    @SerializedName("participants")
    private ArrayList<String> participants;
}
