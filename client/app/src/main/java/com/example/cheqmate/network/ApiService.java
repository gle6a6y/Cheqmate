package com.example.cheqmate.network;

import com.example.cheqmate.dto.ChequeRequest;
import com.example.cheqmate.dto.DebtResponse;
import com.example.cheqmate.dto.GroupCreateRequest;
import com.example.cheqmate.dto.GroupResponse;
import com.example.cheqmate.dto.LoginRequest;
import com.example.cheqmate.dto.LoginResponse;

import java.util.List;

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

    @POST("/api/groups")
    Call<Void> createGroup(@Header("Authorization") String token, @Body GroupCreateRequest request);

    @GET("/api/groups/my")
    Call<List<GroupResponse>> getMyGroups(@Header("Authorization") String token);

    @GET("/api/users/me/debts")
    Call<DebtResponse> getMyDebts(@Header("Authorization") String token);

    @POST("/api/cheques")
    Call<Void> createCheque(@Header("Authorization") String token, @Body ChequeRequest request);
}
