package com.example.cheqmate.network;

import com.example.cheqmate.dto.AchievementResponse;
import com.example.cheqmate.dto.ChequeRequest;
import com.example.cheqmate.dto.ChequeResponse;
import com.example.cheqmate.dto.CreateGameSessionRequest;
import com.example.cheqmate.dto.GameSessionResponse;
import com.example.cheqmate.dto.DebtResponse;
import com.example.cheqmate.dto.GroupCreateRequest;
import com.example.cheqmate.dto.GroupResponse;
import com.example.cheqmate.dto.LoginRequest;
import com.example.cheqmate.dto.LoginResponse;
import com.example.cheqmate.dto.PayDebtRequest;
import com.example.cheqmate.dto.NotificationResponse;
import com.example.cheqmate.dto.RecognizeChequeRequest;
import com.example.cheqmate.dto.RegisterDeviceRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("/api/users/me/groups/{groupId}/debts")
    Call<DebtResponse> getMyDebtsByGroup(@Header("Authorization") String token, @Path("groupId") int groupId);

    @POST("/api/users/me/groups/{groupId}/debts/pay")
    Call<DebtResponse> payDebtInGroup(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Body PayDebtRequest request);

    @POST("/api/cheques")
    Call<Void> createCheque(@Header("Authorization") String token, @Body ChequeRequest request);

    @POST("/api/cheques/recognize")
    Call<String> recognizeCheque(@Header("Authorization") String token, @Body RecognizeChequeRequest request);

    @POST("/api/game-sessions")
    Call<Long> createGameSession(@Header("Authorization") String token, @Body CreateGameSessionRequest request);

    @GET("/api/game-sessions/{id}")
    Call<GameSessionResponse> getGameSession(@Path("id") long sessionId);

    @GET("/api/groups/{id}")
    Call<com.google.gson.JsonObject> getGroupFullInfo(@Header("Authorization") String token, @Path("id") int groupId);

    @GET("/api/groups/{id}/cheques")
    Call<List<ChequeResponse>> getGroupCheques(@Header("Authorization") String token, @Path("id") int groupId);

    @POST("/api/notifications/devices")
    Call<Void> registerDevice(@Header("Authorization") String token, @Body RegisterDeviceRequest request);

    @DELETE("/api/notifications/devices")
    Call<Void> unregisterDevice(@Header("Authorization") String token, @Query("token") String deviceToken);

    @GET("/api/notifications")
    Call<List<NotificationResponse>> getNotifications(@Header("Authorization") String token);

    @GET("/api/notifications/unread-count")
    Call<Map<String, Long>> getUnreadCount(@Header("Authorization") String token);

    @POST("/api/personal-expenses")
    Call<com.example.cheqmate.dto.PersonalExpenseResponse> createPersonalExpense(@Header("Authorization") String token, @Body com.example.cheqmate.dto.PersonalExpenseRequest request);

    @GET("/api/personal-expenses")
    Call<List<com.example.cheqmate.dto.PersonalExpenseResponse>> getPersonalExpenses(@Header("Authorization") String token);

    @DELETE("/api/personal-expenses/{id}")
    Call<Void> deletePersonalExpense(@Header("Authorization") String token, @Path("id") int id);

    @POST("/api/notifications/{id}/read")
    Call<Void> markNotificationRead(@Header("Authorization") String token, @Path("id") int id);

    @GET("/api/achievements/my")
    Call<List<AchievementResponse>> getMyAchievements(@Header("Authorization") String token);

    @GET("/api/stats/my")
    Call<com.example.cheqmate.dto.UserStatsResponse> getMyStats(@Header("Authorization") String token);
}