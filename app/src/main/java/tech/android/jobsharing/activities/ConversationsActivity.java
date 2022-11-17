package tech.android.jobsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tech.android.jobsharing.R;
import tech.android.jobsharing.adapter.RecentConversationAdapter;
import tech.android.jobsharing.models.User;

public class ConversationsActivity extends AppCompatActivity {

    private RecentConversationAdapter recentConversationAdapter;
    private List<User> users;
    private RecyclerView conversationsRecycleView;
    private AppCompatImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        init();
        setListeners();
        getUserDetail();

    }

    private void init(){
        imgBack = findViewById(R.id.imageBack);
        conversationsRecycleView = findViewById(R.id.conversationsRecycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        conversationsRecycleView.setLayoutManager(linearLayoutManager);
        users = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(getApplicationContext(),users);
        conversationsRecycleView.setAdapter(recentConversationAdapter);

    }

    private void setListeners() {
        imgBack.setOnClickListener(v -> onBackPressed());
    }
    //show Toast
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void getUserDetail(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    users.add(user);
                }
                recentConversationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Get list user failed!");
            }
        });
    }







}