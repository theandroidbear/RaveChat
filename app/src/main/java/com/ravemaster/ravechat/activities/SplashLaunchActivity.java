package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

public class SplashLaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_splash_launch);
        PreferenceManager preferenceManager = new PreferenceManager(this);
        new Handler().postDelayed(() -> {
            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }else {
                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            }
        },2000);
    }
}