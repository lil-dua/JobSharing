package tech.android.jobsharing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.ConversationsActivity;
import tech.android.jobsharing.activities.SignInActivity;


/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Home extends Fragment {

    private View view;
    private AppCompatImageView imageViewSignOut,imageViewChat;
    private RecyclerView recyclerView;
    FirebaseAuth mAuth;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        init();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //set event listeners
        setListeners();
        return view;

    }
    private void init(){
        imageViewSignOut = view.findViewById(R.id.imageSignOut);
        imageViewChat = view.findViewById(R.id.imageChat);
        recyclerView = view.findViewById(R.id.newsfeedRecyclerView);
    }
    private void setListeners() {
        imageViewSignOut.setOnClickListener(v -> signOut());
        imageViewChat.setOnClickListener(v -> startActivity(new Intent(getContext(), ConversationsActivity.class)));
    }
    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    //sign out
    private void signOut(){
        mAuth.signOut();
        startActivity(new Intent(getContext(), SignInActivity.class));
    }



}
