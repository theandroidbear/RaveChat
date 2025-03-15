package com.ravemaster.ravechat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravemaster.ravechat.activities.ChatActivity;
import com.ravemaster.ravechat.databinding.UserListItemBinding;
import com.ravemaster.ravechat.models.Users;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private List<Users> usersList = new ArrayList<>();
    private Context context;
    private Activity activity;

    public UsersAdapter( Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setUsersList(List<Users> usersList) {
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserListItemBinding userListItemBinding = UserListItemBinding.inflate(LayoutInflater.from(
                parent.getContext()),
                parent,
                false
        );
        return new UsersViewHolder(userListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        holder.setUserData(usersList.get(position));
        holder.binding.userLayout.setOnClickListener(v->{
            context.startActivity(new Intent(context, ChatActivity.class));
            activity.finish();
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        UserListItemBinding binding;
        UsersViewHolder(UserListItemBinding userListItemBinding) {
            super(userListItemBinding.getRoot());
            binding = userListItemBinding;
        }
        void setUserData(Users users){
            binding.userImage.setImageBitmap(getUserImage(users.getImage()));
            binding.userName.setText(users.getName());
        }
    }

    public Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

