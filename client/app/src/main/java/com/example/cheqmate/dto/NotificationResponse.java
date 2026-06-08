package com.example.cheqmate.dto;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class NotificationResponse {
    @SerializedName("id")
    private Integer id;

    @SerializedName("type")
    private String type;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("groupId")
    private Integer groupId;

    @SerializedName("read")
    private boolean read;

    @SerializedName("createdAt")
    private String createdAt;
}
