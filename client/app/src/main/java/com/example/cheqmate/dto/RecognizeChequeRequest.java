package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecognizeChequeRequest {
    @SerializedName("qr")
    private String qr;
}
