package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.adapters.UsersAdapter;
import com.ravemaster.ravechat.databinding.ActivityUsersBinding;
import com.ravemaster.ravechat.listeners.UserClick;
import com.ravemaster.ravechat.models.User;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity {

    ActivityUsersBinding binding;
    List<User> userList;
    UsersAdapter adapter;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userList = new ArrayList<>();
        adapter = new UsersAdapter(this,userClick);
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
                            User user = new User();
                            user.name = snapshot.getString(Constants.KEY_NAME);
                            user.email = snapshot.getString(Constants.KEY_EMAIL);
                            user.image = snapshot.getString(Constants.KEY_IMAGE);
                            user.token = snapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = snapshot.getId();
                            userList.add(user);
                        }
                        if (!userList.isEmpty()){
                            showUsers(userList);
                        } else {
                            showErrorText();
                        }
                    } else {
                        showErrorText();
                    }
                });
    }

    private void showUsers(List<User> userList) {
        binding.usersRecycler.setVisibility(View.VISIBLE);
        adapter.setUsersList(userList);
        binding.usersRecycler.setAdapter(adapter);
        binding.usersRecycler.setHasFixedSize(true);
        binding.usersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private final UserClick userClick = new UserClick() {
        @Override
        public void onClick(User user) {
            Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
            intent.putExtra(Constants.KEY_USER,user);
            startActivity(intent);
            finish();
        }
    };

    private void isLoading(boolean loading){
        if (loading){
            binding.loadingUsers.setVisibility(View.VISIBLE);
        } else {
            binding.loadingUsers.setVisibility(View.GONE);
        }
    }

    private void showErrorText(){
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

}