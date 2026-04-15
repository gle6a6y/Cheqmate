package com.example.cheqmate.network;

import com.example.cheqmate.dto.LoginRequest;
import com.example.cheqmate.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<Void> register(@Body LoginRequest request);
}
