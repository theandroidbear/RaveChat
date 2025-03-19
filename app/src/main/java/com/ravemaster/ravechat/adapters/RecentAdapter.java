package com.ravemaster.ravechat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravemaster.ravechat.databinding.RecentChatsListItemBinding;
import com.ravemaster.ravechat.listeners.ConversationClick;
import com.ravemaster.ravechat.models.ChatMessage;
import com.ravemaster.ravechat.models.User;

import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.RecentViewHolder> {

    private final List<ChatMessage> messages;
    Context context;
    private ConversationClick conversationClick;

    public RecentAdapter(Context context, List<ChatMessage> messages, ConversationClick conversationClick) {
        this.context = context;
        this.messages = messages;
        this.conversationClick = conversationClick;
    }


    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentViewHolder(
                RecentChatsListItemBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        holder.setData(messages.get(position));
        holder.binding.userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.id = messages.get(holder.getAdapterPosition()).conversationId;
                user.image = messages.get(holder.getAdapterPosition()).conversationImage;
                user.name = messages.get(holder.getAdapterPosition()).conversationName;
                conversationClick.onClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class RecentViewHolder extends RecyclerView.ViewHolder {
        RecentChatsListItemBinding binding;
        public RecentViewHolder(RecentChatsListItemBinding recentChatsListItemBinding) {
            super(recentChatsListItemBinding.getRoot());
            binding = recentChatsListItemBinding;
        }
        void setData(ChatMessage message){
            binding.convoImage.setImageBitmap(getUserImage(message.conversationImage));
            binding.convoMessage.setText(message.message);
            binding.convoName.setText(message.conversationName);
        }
    }


    public Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
