
package com.example.releaft.data;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PATCH;

public interface ProfileUpdateApi {

    @PATCH("api/profile/me/")
    Call<ResponseBody> updateProfile(
            @Header("Authorization") String authorization,
            @Body Map<String, String> profileData
    );
}
