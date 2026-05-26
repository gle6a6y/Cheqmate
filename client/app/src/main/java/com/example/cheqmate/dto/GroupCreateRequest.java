package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupCreateRequest {
    @SerializedName("groupName")
    private String groupName;

    @SerializedName("memberNames")
    private List<String> memberNames;
}
