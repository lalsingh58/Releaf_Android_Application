package com.example.releaft.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface StatsApi {

    @GET("api/stats/me/")
    Call<ResponseBody> getMyStats(
            @Header("Authorization") String authorization
    );
}