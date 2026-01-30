package com.ignishers.milkmanager2.data.remote;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static final String SUPABASE_URL = SupabaseConfig.SUPABASE_URL; 
    private static final String SUPABASE_KEY = SupabaseConfig.SUPABASE_KEY;

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = createClient(SUPABASE_KEY); // Default Anon Client
        }
        return retrofit;
    }

    // NEW: Client for Authenticated Requests (Admin/Seller actions)
    public static Retrofit getAuthenticatedClient(String accessToken) {
        return createClient(accessToken);
    }

    private static Retrofit createClient(String outputToken) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request().newBuilder()
                            .addHeader("apikey", SUPABASE_KEY) // Always needed
                            .addHeader("Authorization", "Bearer " + outputToken) // User Token OR Anon Key
                            .addHeader("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
