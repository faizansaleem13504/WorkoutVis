package com.example.workoutvisdraft;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ThreadClass extends Thread {
    APIData apiData;
    ThreadClass(APIData s){
        apiData=s;
    }
    @Override
    public void run() {
        Retrofit retrofit=new Retrofit.Builder().baseUrl("https://workoutvis.uc.r.appspot.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OurRetrofit ourRetrofit=retrofit.create(OurRetrofit.class);
        Call<APIRes> call=ourRetrofit.PostData(apiData);
        call.enqueue(new Callback<APIRes>() {
            @Override
            public void onResponse(Call<APIRes> call, Response<APIRes> response) {
                System.out.println(response.body());
            }

            @Override
            public void onFailure(Call<APIRes> call, Throwable t) {
                System.out.println("Failed");
            }
        });
    }
}
