package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

public class SplashLaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_splash_launch);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
        },2000);
    }
}