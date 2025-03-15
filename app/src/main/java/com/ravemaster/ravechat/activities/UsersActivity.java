package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.adapters.UsersAdapter;
import com.ravemaster.ravechat.databinding.ActivityUsersBinding;
import com.ravemaster.ravechat.models.Users;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    ActivityUsersBinding binding;
    List<Users> usersList;
    UsersAdapter adapter;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        usersList = new ArrayList<>();
        adapter = new UsersAdapter(this,this);
        preferenceManager = new PreferenceManager(this);
        getUsers();

        binding.friendsToolBar.setNavigationOnClickListener(v->{
            Intent intent = new Intent(UsersActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getUsers(){
        isLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    isLoading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot snapshot:
                                task.getResult()) {
                            if (currentUserId.equalsIgnoreCase(snapshot.getId())){
                                continue;
                            }
                            Users user = new Users(snapshot.getString(Constants.KEY_NAME),snapshot.getString(Constants.KEY_EMAIL),snapshot.getString(Constants.KEY_IMAGE),snapshot.getString(Constants.KEY_FCM_TOKEN));
                            usersList.add(user);
                        }
                        if (!usersList.isEmpty()){
                            showUsers(usersList);
                        } else {
                            showErrorText();
                        }
                    } else {
                        showErrorText();
                    }
                });
    }

    private void showUsers(List<Users> usersList) {
        binding.usersRecycler.setVisibility(View.VISIBLE);
        adapter.setUsersList(usersList);
        binding.usersRecycler.setAdapter(adapter);
        binding.usersRecycler.setHasFixedSize(true);
        binding.usersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.loadingUsers.setVisibility(View.VISIBLE);
        } else {
            binding.loadingUsers.setVisibility(View.GONE);
        }
    }

    private void showErrorText(){
        binding.usersErrorMessage.setText("Error loading users");
        binding.usersErrorMessage.setVisibility(View.VISIBLE);
    }

}