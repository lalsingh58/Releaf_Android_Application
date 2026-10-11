package com.example.releaft.data;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ActionApi {

    @Multipart
    @POST("api/actions/submit/")
    Call<ResponseBody> submitAction(
            @Header("Authorization") String authorization,
            @Part("action_type") RequestBody actionType,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );

    @GET("api/actions/my/")
    Call<ResponseBody> getMyActions(
            @Header("Authorization") String authorization
    );
}
