package com.ravemaster.ravechat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.adapters.ChatAdapter;
import com.ravemaster.ravechat.api.RequestManager;
import com.ravemaster.ravechat.api.interfaces.FCMListener;
import com.ravemaster.ravechat.api.models.FCMResponse;
import com.ravemaster.ravechat.databinding.ActivityChatBinding;
import com.ravemaster.ravechat.models.ChatMessage;
import com.ravemaster.ravechat.models.User;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.JsonKey;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    User receiver;
    List<ChatMessage>messages;
    ChatAdapter adapter;
    PreferenceManager preferenceManager;
    FirebaseFirestore database;
    String conversationId = null;
    boolean isReceiverAvailable = false;
    RequestManager manager;
    String accessToken;
    String lastOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceiverDetails();
        init(this);
        listenMessages();

        binding.chatBack.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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
        message.put(Constants.KEY_RECEIVER_ID,receiver.id);
        message.put(Constants.KEY_MESSAGE,binding.enterMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversation(binding.enterMessage.getText().toString());
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            map.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            map.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            map.put(Constants.KEY_RECEIVER_ID, receiver.id);
            map.put(Constants.KEY_RECEIVER_NAME, receiver.name);
            map.put(Constants.KEY_RECEIVER_IMAGE, receiver.image);
            map.put(Constants.KEY_LAST_MESSAGE,binding.enterMessage.getText().toString());
            map.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(map);
        }
        if (!isReceiverAvailable){
            sendNotification(receiver.id, preferenceManager.getString(Constants.KEY_NAME), binding.enterMessage.getText().toString());
        }
        binding.enterMessage.setText(null);
    }

    private void listenMessages (){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiver.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiver.id)
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
                int previousCount = messages.size();
                for (DocumentChange documentChange: value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.time = getReadableTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        messages.add(chatMessage);
                    }
                }
                Collections.sort(messages, (obj1, obj2)->obj1.date.compareTo(obj2.date));
                int newCount = messages.size();
                if (previousCount == 0){
                    adapter.notifyDataSetChanged();
                } else if (newCount>previousCount) {
                    adapter.notifyItemRangeInserted(previousCount,newCount-previousCount);
                }
                if (!messages.isEmpty()){
                    binding.chatRecycler.smoothScrollToPosition(messages.size() - 1);
                }
                binding.chatRecycler.setVisibility(View.VISIBLE);
            }
            binding.chatProgress.setVisibility(View.GONE);
            if(conversationId == null){
                checkForConversation();
            }
        }
    };

    private void sendNotification(String userId, String name, String text){
        new Thread(()->{
            try {
                InputStream serviceAccountStream = getResources().openRawResource(JsonKey.jsonKey);
                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(serviceAccountStream)
                        .createScoped(Collections.singleton(Constants.SCOPE));
                credentials.refreshIfExpired();
                accessToken = credentials.getAccessToken().getTokenValue();
                getNotificationToken(userId,name,text);

            } catch (Exception e){
                //This is the error origin
                Log.d("Notification error",e.getMessage());
            }
        }).start();
    }

    private void getNotificationToken(String userId, String name, String text){
        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String targetToken = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                    if (targetToken != null) {
                        manager.sendMessage(fcmListener,targetToken,accessToken,name,text);
                    } else {
                        showToasts("Target token not found!");
                    }
                })
                .addOnFailureListener(e->{
                    showToasts(e.getMessage());
                });
    }

    private final FCMListener fcmListener = new FCMListener() {
        @Override
        public void unSuccess(FCMResponse response, String message) {
        }

        @Override
        public void onFailed(String message) {
            Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private void addConversation(HashMap<String, Object> conversation){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> {
                    conversationId = documentReference.getId();
                });
    }

    private void updateConversation(String message){
        DocumentReference reference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        reference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation(){
        if (!messages.isEmpty()){
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiver.id
            );
            checkForConversationRemotely(
                    receiver.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> onCompleteListener = new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()){
                DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);
                conversationId = snapshot.getId();
            }
        }
    };

    private void init(Context context){
        manager = new RequestManager(context);
        preferenceManager = new PreferenceManager(context);
        messages = new ArrayList<>();
        adapter = new ChatAdapter(context,preferenceManager.getString(Constants.KEY_USER_ID),messages);
        binding.chatRecycler.setAdapter(adapter);
        binding.chatRecycler.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        database = FirebaseFirestore.getInstance();
    }

    private void listenReceiverAvailability(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiver.id
        ).addSnapshotListener(ChatActivity.this,(value, error)->{
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                    lastOnline = value.getString(Constants.LAST_ONLINE);
                }
                receiver.token = value.getString(Constants.KEY_FCM_TOKEN);
            }
            if (isReceiverAvailable){
                binding.chatAvailability.setVisibility(View.VISIBLE);
                binding.chatAvailability.setText("Online");
            } else {
                if (lastOnline == null){
                    binding.chatAvailability.setVisibility(View.GONE);
                } else {
                    binding.chatAvailability.setText("Last seen on "+lastOnline);
                }
            }
        });
    }

    private void loadReceiverDetails() {
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.chatImage.setImageBitmap(getUserImage(receiver.image));
        binding.chatName.setText(receiver.name);
    }

    public Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String getReadableTime(Date date){
        return new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenReceiverAvailability();
        if (!messages.isEmpty()) {
            adapter.setMessages(messages);
            binding.chatRecycler.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}