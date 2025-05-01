package com.ravemaster.ravechat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ravemaster.ravechat.databinding.ListContainerReceiverMessageBinding;
import com.ravemaster.ravechat.databinding.ListContainerSentMessageBinding;
import com.ravemaster.ravechat.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messages;
    private Context context;
    private String senderId;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(Context context, String senderId, List<ChatMessage> messages) {
        this.context = context;
        this.senderId = senderId;
        this.messages = messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SenderMessageViewHolder(
                    ListContainerSentMessageBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceiverMessageViewHolder(
                    ListContainerReceiverMessageBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SenderMessageViewHolder) holder).setData(messages.get(position));
        } else {
            ((ReceiverMessageViewHolder) holder).setData(messages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SenderMessageViewHolder extends RecyclerView.ViewHolder{
        private final ListContainerSentMessageBinding binding;
        public SenderMessageViewHolder(ListContainerSentMessageBinding listContainerSentMessageBinding) {
            super(listContainerSentMessageBinding.getRoot());
            binding = listContainerSentMessageBinding;
        }
        void setData(ChatMessage message){
            binding.senderMessage.setText(message.message);
            binding.senderTime.setText(message.time);
        }
    }
    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ListContainerReceiverMessageBinding binding;
        public ReceiverMessageViewHolder(ListContainerReceiverMessageBinding listContainerReceiverMessageBinding) {
            super(listContainerReceiverMessageBinding.getRoot());
            binding = listContainerReceiverMessageBinding;
        }
        void setData(ChatMessage message){
            binding.receiverMessage.setText(message.message);
            binding.receiverTime.setText(message.time);
        }
    }
}
