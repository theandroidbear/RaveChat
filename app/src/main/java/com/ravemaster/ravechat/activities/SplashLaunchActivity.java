package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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