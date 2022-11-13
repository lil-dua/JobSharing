package tech.android.jobsharing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import tech.android.jobsharing.databinding.ActivityConversationsBinding;

public class ConversationsActivity extends AppCompatActivity {

    private ActivityConversationsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivityConversationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setListeners
        setListeners();
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}