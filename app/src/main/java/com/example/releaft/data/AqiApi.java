
package com.example.releaft.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AqiApi {

    @GET("api/aqi/")
    Call<ResponseBody> getAqi(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude
    );
}
