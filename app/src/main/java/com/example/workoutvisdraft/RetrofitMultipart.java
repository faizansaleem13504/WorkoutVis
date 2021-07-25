package com.example.workoutvisdraft;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitMultipart {
    @Multipart
    @POST("/run_model")
    Call<APIRes> uploadImage(@Part("data") RequestBody desc);
}
