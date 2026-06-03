package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("password")
    private String password;

}