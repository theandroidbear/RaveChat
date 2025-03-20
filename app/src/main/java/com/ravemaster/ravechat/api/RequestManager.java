package com.ravemaster.ravechat.api;

import android.content.Context;

import com.ravemaster.ravechat.api.interfaces.FCMApiService;
import com.ravemaster.ravechat.api.interfaces.FCMListener;
import com.ravemaster.ravechat.api.models.FCMMessage;
import com.ravemaster.ravechat.api.models.FCMResponse;
import com.ravemaster.ravechat.api.models.Message;
import com.ravemaster.ravechat.api.models.Notification;
import com.ravemaster.ravechat.utilities.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RequestManager {

    Context context;

    public RequestManager(Context context) {
        this.context = context;
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public void sendMessage(FCMListener listener, String FCMToken, String accessToken, String name, String text){
        FCMApiService apiService = retrofit.create(FCMApiService.class);
        Notification notification = new Notification(name, text);
        Message message = new Message(FCMToken, notification);
        FCMMessage fcmMessage = new FCMMessage(message);

        Call<FCMResponse> call = apiService.sendMessage("Bearer "+accessToken,fcmMessage);
        call.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (!response.isSuccessful()){
                    listener.onFailed(response.message()+" from onResponse");
                    return;
                }
                listener.unSuccess(response.body(), response.message());

            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable throwable) {
                listener.onFailed(throwable.getMessage()+" from onFailure");
            }
        });

    }
}
