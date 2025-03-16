package com.ravemaster.ravechat.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.adapters.ChatAdapter;
import com.ravemaster.ravechat.databinding.ActivityChatBinding;
import com.ravemaster.ravechat.models.ChatMessage;
import com.ravemaster.ravechat.models.User;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    User receiver;
    List<ChatMessage>messages;
    ChatAdapter adapter;
    PreferenceManager preferenceManager;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadReceiverDetails();
        init(this);
        listenMessages();

        if (messages.size()>1){
            adapter.notifyDataSetChanged();
            binding.chatRecycler.smoothScrollToPosition(messages.size() - 1);
        }

        binding.chatBack.setOnClickListener(v->{
            onBackPressed();
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void sendMessage(){
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiver.getId());
        message.put(Constants.KEY_MESSAGE,binding.enterMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        binding.enterMessage.setText(null);
    }

    private void listenMessages (){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiver.getId())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiver.getId())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if (error != null){
                return;
            }
            if (value != null){
                int count = messages.size();
                for (DocumentChange documentChange: value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        ChatMessage chatMessage = new ChatMessage(
                                documentChange.getDocument().getString(Constants.KEY_SENDER_ID),
                                documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID),
                                documentChange.getDocument().getString(Constants.KEY_MESSAGE),
                                getReadableTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)),
                                documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)
                        );
                        messages.add(chatMessage);
                    }
                }
                Collections.sort(messages,(obj1, obj2) -> obj1.getDate().compareTo(obj2.getDate()));
                if (count == 0){
                    adapter.setMessages(messages);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.setMessages(messages);
                    adapter.notifyItemRangeInserted(messages.size(),messages.size());
                    binding.chatRecycler.smoothScrollToPosition(messages.size() -1);
                }
                binding.chatRecycler.setVisibility(View.VISIBLE);
            }
            binding.chatProgress.setVisibility(View.GONE);
        }
    };

    private void init(Context context){
        preferenceManager = new PreferenceManager(context);
        messages = new ArrayList<>();
        adapter = new ChatAdapter(context,preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycler.setAdapter(adapter);
        binding.chatRecycler.setLayoutManager(new LinearLayoutManager(context));
        database = FirebaseFirestore.getInstance();
    }

    private void loadReceiverDetails() {
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.chatImage.setImageBitmap(getUserImage(receiver.getImage()));
        binding.chatName.setText(receiver.getName());
    }

    public Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private String getReadableTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messages.size()>1){
            adapter.notifyDataSetChanged();
            binding.chatRecycler.smoothScrollToPosition(messages.size()-1);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (messages.size()>1){
            adapter.notifyDataSetChanged();
            binding.chatRecycler.smoothScrollToPosition(messages.size()-1);
        }
    }
}