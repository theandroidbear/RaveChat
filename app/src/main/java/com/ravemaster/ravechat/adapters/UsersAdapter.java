package com.ravemaster.ravechat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravemaster.ravechat.databinding.UserListItemBinding;
import com.ravemaster.ravechat.listeners.UserClick;
import com.ravemaster.ravechat.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private List<User> userList = new ArrayList<>();
    private Context context;
    UserClick userClick;

    public UsersAdapter( Context context, UserClick userClick) {
        this.context = context;
        this.userClick = userClick;
    }

    public void setUsersList(List<User> userList) {
        this.userList = userList;
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
        holder.setUserData(userList.get(position));
        holder.binding.userLayout.setOnClickListener(v->{
            userClick.onClick(userList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {
        UserListItemBinding binding;
        UsersViewHolder(UserListItemBinding userListItemBinding) {
            super(userListItemBinding.getRoot());
            binding = userListItemBinding;
        }
        void setUserData(User user){
            binding.userImage.setImageBitmap(getUserImage(user.getImage()));
            binding.userName.setText(user.getName());
        }
    }

    public Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

