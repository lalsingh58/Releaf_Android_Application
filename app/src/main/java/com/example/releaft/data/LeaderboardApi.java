
package com.example.releaft.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface LeaderboardApi {

    @GET("api/leaderboard/")
    Call<ResponseBody> getLeaderboard(
            @Header("Authorization") String authorization
    );
}
