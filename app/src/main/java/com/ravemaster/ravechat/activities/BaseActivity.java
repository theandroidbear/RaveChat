package com.ravemaster.ravechat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    DocumentReference documentReference;
    boolean isOnline = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(this);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        documentReference.update(Constants.KEY_AVAILABILITY,1);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isOnline){
            documentReference.update(Constants.KEY_AVAILABILITY,1);
            isOnline = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isOnline){
            documentReference.update(Constants.KEY_AVAILABILITY,0);
            documentReference.update(Constants.LAST_ONLINE,getReadableTime(new Date()));
            isOnline = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnline){
            documentReference.update(Constants.KEY_AVAILABILITY,1);
            isOnline = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isOnline){
            documentReference.update(Constants.KEY_AVAILABILITY,0);
            documentReference.update(Constants.LAST_ONLINE,getReadableTime(new Date()));
            isOnline = false;
        }
    }

    private String getReadableTime(Date date){
        return new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date);
    }
}
