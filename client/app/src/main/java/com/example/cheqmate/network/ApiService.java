package com.example.cheqmate.network;

import com.example.cheqmate.dto.FortuneWheelRequest;
import com.example.cheqmate.dto.LoginRequest;
import com.example.cheqmate.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<Void> register(@Body LoginRequest request);

    @POST("/api/cheques/fortune-wheel")
    Call<Void> playFortuneWheel(@Header("Authorization") String token, @Body FortuneWheelRequest request);
    
    @GET("/api/fortune-wheel/test")
    Call<String> testFortuneWheel();

    @POST("/api/fortune-wheel/spin")
    Call<String> spinFortuneWheel(@Body FortuneWheelRequest request);
}
