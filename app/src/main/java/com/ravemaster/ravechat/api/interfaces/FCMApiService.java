package com.ravemaster.ravechat.api.interfaces;

import com.ravemaster.ravechat.api.models.FCMMessage;
import com.ravemaster.ravechat.api.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FCMApiService {
    @POST("v1/projects/ravechat-ef4ca/messages:send")
    Call<FCMResponse> sendMessage(
            @Header("Authorization") String authorization,
            @Body FCMMessage message
    );
}
