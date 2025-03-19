package com.ravemaster.ravechat.api;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RequestManager {

    Context context;

    public RequestManager(Context context) {
        this.context = context;
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/fcm/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build();

}
