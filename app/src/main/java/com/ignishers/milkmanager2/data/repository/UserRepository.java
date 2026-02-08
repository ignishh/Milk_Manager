package com.ignishers.milkmanager2.data.repository;

import com.google.gson.JsonObject;
import com.ignishers.milkmanager2.data.model.UserAccount;
import com.ignishers.milkmanager2.data.remote.SupabaseClient;
import com.ignishers.milkmanager2.data.remote.api.UserApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private UserApi userApi;

    public UserRepository() {
        userApi = SupabaseClient.getClient().create(UserApi.class);
    }

    public interface LoginCallback {
        void onSuccess(String token, String userId);
        void onFailure(String error);
    }

    private static String currentUserToken = null;

    public void login(String email, String password, LoginCallback callback) {
        JsonObject credentials = new JsonObject();
        credentials.addProperty("email", email);
        credentials.addProperty("password", password);

        userApi.login(credentials).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Supabase Returns: { "access_token": "...", "user": { "id": "..." } }
                        String token = response.body().get("access_token").getAsString();
                        String userId = response.body().get("user").getAsJsonObject().get("id").getAsString();
                        
                        // Save Token for future calls
                        currentUserToken = token;
                        
                        callback.onSuccess(token, userId);
                    } catch (Exception e) {
                        callback.onFailure("Parsing Error: " + e.getMessage());
                    }
                } else {
                    String errorMsg = "Login Failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            // Supabase usually returns: {"error":"invalid_grant","error_description":"Invalid login credentials"}
                            // Simple parsing to avoid complex Gson type adapters for simple error
                            if (errorStr.contains("error_description")) {
                                errorMsg = errorStr.split("\"error_description\":\"")[1].split("\"")[0];
                            } else if (errorStr.contains("msg")) {
                                errorMsg = errorStr.split("\"msg\":\"")[1].split("\"")[0];
                            } else {
                                errorMsg += " " + errorStr;
                            }
                        }
                    } catch (Exception e) {
                        errorMsg += " (Unknown Error)";
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onFailure("Network Error: " + t.getMessage());
            }
        });
    }

    public interface UserDetailsCallback {
        void onUserLoaded(UserAccount user);
        void onError(String error);
    }

    public void fetchUserDetails(String authUid, UserDetailsCallback callback) {
        // "select=*" tells Supabase to return all columns
        userApi.getUserDetails("eq." + authUid, "*").enqueue(new Callback<List<UserAccount>>() {
            @Override
            public void onResponse(Call<List<UserAccount>> call, Response<List<UserAccount>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onUserLoaded(response.body().get(0));
                } else {
                    callback.onError("User Details not found for ID: " + authUid);
                }
            }

            @Override
            public void onFailure(Call<List<UserAccount>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface CreateSellerCallback {
        void onSuccess();
        void onError(String error);
    }

    public void createSeller(String email, String password, String name, String mobile, String price, String date, CreateSellerCallback callback) {
        if (currentUserToken == null) {
            callback.onError("Session Expired. Please Login Again.");
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("email_input", email);
        data.addProperty("password_input", password);
        data.addProperty("name_input", name);
        data.addProperty("mobile_input", mobile);
        data.addProperty("price_input", Double.parseDouble(price)); // Ensure Numeric
        data.addProperty("effective_date_input", date);

        // USE AUTHENTICATED CLIENT
        UserApi authApi = SupabaseClient.getAuthenticatedClient(currentUserToken).create(UserApi.class);

        authApi.createAuthUser(data).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorMsg = "Creation Failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    public interface SellersListCallback {
        void onSuccess(List<UserAccount> sellers);
        void onError(String error);
    }

    public void getAllSellers(SellersListCallback callback) {
        if (currentUserToken == null) {
            callback.onError("Session Expired");
            return;
        }
        UserApi authApi = SupabaseClient.getAuthenticatedClient(currentUserToken).create(UserApi.class);
        authApi.getAllSellers().enqueue(new Callback<List<UserAccount>>() {
            @Override
            public void onResponse(Call<List<UserAccount>> call, Response<List<UserAccount>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch sellers: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<UserAccount>> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    public void toggleSellerStatus(String userId, boolean makeActive, CreateSellerCallback callback) {
        if (currentUserToken == null) {
            callback.onError("Session Expired");
            return;
        }
        UserApi authApi = SupabaseClient.getAuthenticatedClient(currentUserToken).create(UserApi.class);
        
        JsonObject update = new JsonObject();
        update.addProperty("status", makeActive ? "ACTIVE" : "INACTIVE");
        
        // Supabase Patch requires "eq." prefix for query param usually if passed raw, 
        // but Retrofit @Query handles the param. 
        // Wait, Supabase syntax is usually ?user_id=eq.UUID
        authApi.updateUserStatus("eq." + userId, update).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Update Failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
