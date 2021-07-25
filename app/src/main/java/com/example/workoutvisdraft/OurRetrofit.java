package com.example.workoutvisdraft;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OurRetrofit {
    @POST("/run_model")
    Call<APIRes> PostData(@Body APIData res);
}
