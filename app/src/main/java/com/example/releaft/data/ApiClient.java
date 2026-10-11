
package com.example.releaft.data;

import retrofit2.Retrofit;
//import com.example.releaft.data.ActionApi;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "http://127.0.0.1:8000/";

    private static final Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();

    public static AuthApi getAuthApi() {
        return retrofit.create(AuthApi.class);
    }

    public static ProfileApi getProfileApi() {
        return retrofit.create(ProfileApi.class);
    }

    public static ProfileUpdateApi getProfileUpdateApi() {
        return retrofit.create(ProfileUpdateApi.class);
    }

    public static AqiApi getAqiApi() {
        return retrofit.create(AqiApi.class);
    }
    public static StatsApi getStatsApi() {
        return retrofit.create(StatsApi.class);
    }

    public static ActionApi getActionApi() {
        return retrofit.create(ActionApi.class);
    }


    public static LeaderboardApi getLeaderboardApi() {
        return retrofit.create(LeaderboardApi.class);
    }


}
