
package com.example.releaft.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login/")
    Call<TokenResponse> login(
            @Body LoginRequest request
    );

    @POST("api/auth/register/")
    Call<TokenResponse> register(
            @Body RegisterRequest request
    );
}
