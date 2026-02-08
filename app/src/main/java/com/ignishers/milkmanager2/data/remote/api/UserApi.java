package com.ignishers.milkmanager2.data.remote.api;

import com.google.gson.JsonObject;
import com.ignishers.milkmanager2.data.model.UserAccount;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface UserApi {

    // Login: Supabase Auth (Sign In with Password)
    // NOTE: Supabase Auth API is typically: /auth/v1/token?grant_type=password
    @POST("auth/v1/token?grant_type=password")
    Call<JsonObject> login(@Body JsonObject credentials);

    // Get User Details from our custom table 'user_account' after Auth
    @GET("rest/v1/user_account")
    Call<List<UserAccount>> getUserDetails(@Query("auth_uid") String authUid, @Query("select") String select);
    
    // Create new User Account in our custom table
    @POST("rest/v1/user_account")
    Call<Void> createUserAccount(@Body UserAccount user);

    // Call Database Function (RPC) to create Auth User
    @POST("rest/v1/rpc/create_user_by_admin")
    Call<JsonObject> createAuthUser(@Body JsonObject userData);

    // Get All Sellers
    @GET("rest/v1/user_account?role=eq.SELLER&select=*")
    Call<List<UserAccount>> getAllSellers();

    // Update User Status
    @PATCH("rest/v1/user_account")
    Call<Void> updateUserStatus(@Query("user_id") String userId, @Body JsonObject statusUpdate);
}
