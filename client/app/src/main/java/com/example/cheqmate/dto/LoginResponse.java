package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("name")
    private String name;
}