package tech.android.jobsharing.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.android.jobsharing.adapter.ChatAdapter;
import tech.android.jobsharing.databinding.ActivityChatBinding;
import tech.android.jobsharing.models.ChatMessage;
import tech.android.jobsharing.models.User;
import tech.android.jobsharing.network.ApiClient;
import tech.android.jobsharing.network.ApiService;
import tech.android.jobsharing.utils.Constant;


public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private FirebaseUser mUser;
    private DatabaseReference databaseReference;
    private String receiverFcmToken;
    private String receiverId = null;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessageList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //init
        init();
        //get user details
        getUserDetails();
        //set Listeners
        setListeners();

    }

    private void init() {
        chatMessageList = new ArrayList<>();
        apiService = ApiClient.getClient().create(ApiService.class);
    }


    private void getUserDetails() {
        receiverId = getIntent().getStringExtra("userId");
        //get user details
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(receiverId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                   User user = snapshot.getValue(User.class);
                   binding.textName.setText(user.getName());
                   receiverFcmToken = user.getFcmToken();
                   readMessage(mUser.getUid(),receiverId,user.getImage());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> {
            String msg = binding.inputMessage.getText().toString();
            if (!msg.equals("")){
                sendMessage(mUser.getUid(), receiverId,msg);
            }else {
                showToast("Empty message!");
            }
            binding.inputMessage.setText("");
        });
    }


    // send message
    private void sendMessage(String sender, final String receiver, String message){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        databaseReference.child("Chats").push().setValue(hashMap);

        final String msg = message;
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                //push notification
                try {
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverFcmToken);

                    JSONObject data = new JSONObject();
                    data.put("userId",user.getUserId());
                    data.put("name",user.getName());
                    data.put("fcmToken",user.getFcmToken());
                    data.put("message",msg);

                    JSONObject body = new JSONObject();
                    body.put("data",data);
                    body.put("registration_ids",tokens);

                    sendNotification(body.toString());
                    showToast("Sent notification successfully!");
                }catch (Exception exception){
                    showToast("JSON:"+exception.getMessage());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //read message
    private void readMessage(final String senderId, final String receiverId, final String receiverImage){
        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessageList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(chatMessage.getReceiver().equals(senderId) && chatMessage.getSender().equals(receiverId)
                            || chatMessage.getReceiver().equals(receiverId) && chatMessage.getSender().equals(senderId)){
                        chatMessageList.add(chatMessage);
                    }
                    chatAdapter = new ChatAdapter(chatMessageList,getBitmapFromEncodedString(receiverImage));
                    binding.chatRecycleView.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();

                }
                binding.chatRecycleView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //send notification
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constant.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    try {
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // encoded image
    private Bitmap getBitmapFromEncodedString(String encodeImage){
        if(encodeImage != null){
            byte[] bytes = Base64.decode(encodeImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }else {
            return null;
        }
    }
}