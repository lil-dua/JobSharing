package tech.android.jobsharing.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tech.android.jobsharing.adapter.RecentConversationAdapter;
import tech.android.jobsharing.adapter.SearchUserAdapter;
import tech.android.jobsharing.databinding.ActivityConversationsBinding;
import tech.android.jobsharing.models.User;

public class ConversationsActivity extends AppCompatActivity {

    private ActivityConversationsBinding binding;
    private RecentConversationAdapter recentConversationAdapter;
    private SearchUserAdapter searchUserAdapter;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUserDetail();
        setListeners();
    }

    private void init(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.conversationsRecycleView.setLayoutManager(linearLayoutManager);
        users = new ArrayList<>();
        recentConversationAdapter = new RecentConversationAdapter(getApplicationContext(),users);
        binding.conversationsRecycleView.setAdapter(recentConversationAdapter);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageSearchUser.setOnClickListener(v -> {
            processSearch(binding.editTextSearchUser.getText().toString());
        });
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
                users.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        users.add(user);
                    }
                }
                recentConversationAdapter.notifyDataSetChanged();
                binding.conversationsRecycleView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Get list user failed!");
            }
        });
    }

    //search user list
    private void processSearch(String s){
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(FirebaseDatabase.getInstance().getReference()
                                .child("Users").orderByChild("name")
                                .startAt(s).endAt(s+"\uf8ff"), User.class)
                        .build();
        searchUserAdapter = new SearchUserAdapter(getApplicationContext(),options);
        searchUserAdapter.startListening();
        binding.conversationsRecycleView.setAdapter(searchUserAdapter);
    }







}