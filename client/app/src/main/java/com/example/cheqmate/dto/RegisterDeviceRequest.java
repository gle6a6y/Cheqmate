package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterDeviceRequest {
    @SerializedName("token")
    private String token;

    @SerializedName("platform")
    private String platform;
}
