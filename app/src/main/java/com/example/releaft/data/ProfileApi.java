
package com.example.releaft.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ProfileApi {

    @GET("api/profile/me/")
    Call<ResponseBody> getMyProfile(
            @Header("Authorization") String authorization
    );
}
