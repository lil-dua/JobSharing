package tech.android.jobsharing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;

import tech.android.jobsharing.databinding.ActivityNewPostBinding;

public class NewPostActivity extends AppCompatActivity {

    private ActivityNewPostBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivityNewPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //set event listener
        setListeners();
    }
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> DialogCancel());
        //done
        binding.imageDone.setOnClickListener(v -> onBackPressed());
    }

    private void DialogCancel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to remove this post?");
        //Confirm:
        builder.setNegativeButton("Remove",(dialogInterface, i) -> onBackPressed());
        //Stay:
        builder.setPositiveButton("Continue",(dialogInterface, i) -> {
            //Stay
        });
        builder.show();
    }
}