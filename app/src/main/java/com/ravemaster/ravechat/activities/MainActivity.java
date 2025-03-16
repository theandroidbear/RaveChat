package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.databinding.ActivityMainBinding;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    PreferenceManager preferenceManager;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        getToken();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnViewUsers.setOnClickListener(v->{
            startActivity(new Intent(this, UsersActivity.class));
        });

        setSupportActionBar(binding.myToolBar);

//        binding.myToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == R.id.more){
//                    showPopUpMenu(binding.myToolBar);
//                    return true;
//                } else {
//                    return false;
//                }
//
//            }
//        });

    }

    private void showPopUpMenu(MaterialToolbar myToolBar) {
        PopupMenu popupMenu = new PopupMenu(this,myToolBar);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu,popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.myAccount){
                    showToasts("Navigate to my profile activity");
                    return true;
                } else if(item.getItemId() == R.id.logOut){
                    signOut();
                    return true;
                } else {
                    return false;
                }
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.myAccount) {
            showToasts("My account");
            return true;
        } else if (id == R.id.logOut) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        showToasts("Logging out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        reference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToasts("Unable to log you out");
                });
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(exception->{
                    showToasts("Unable to update token");
                });
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}