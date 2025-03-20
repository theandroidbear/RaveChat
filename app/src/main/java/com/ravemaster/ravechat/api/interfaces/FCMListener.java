package com.ravemaster.ravechat.api.interfaces;

import com.ravemaster.ravechat.api.models.FCMResponse;

public interface FCMListener {
    void unSuccess(FCMResponse response, String message);
    void onFailed(String message);
}
