package tech.android.jobsharing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import tech.android.jobsharing.databinding.ActivityEditPrivateDetailsBinding;
import tech.android.jobsharing.models.User;

public class EditPrivateDetailsActivity extends AppCompatActivity {

    private ActivityEditPrivateDetailsBinding binding;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivityEditPrivateDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //get user details
        getUserDetails();
        //set listener
        setListeners();
    }

    private void setListeners() {
        //back press
        binding.imageBack.setOnClickListener(v -> DialogCancel());
        //done
        binding.imageDone.setOnClickListener(v -> updateUserDetails());

    }

    private void getUserDetails(){
        if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    binding.editTextEmail.setText(user.getEmail());
                    binding.editTextPhone.setText(user.getPhone());
                    binding.editTextDate.setText(user.getDate());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            showToast("User id was not found!");
        }
    }

    private void updateUserDetails(){
        String email = binding.editTextEmail.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String date = binding.editTextDate.getText().toString().trim();
        if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    databaseReference.child("email").setValue(email);
                    databaseReference.child("phone").setValue(phone);
                    databaseReference.child("date").setValue(date);
                    showToast("Private details update successfully!");
                    startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            showToast("User id was not found!");
        }
    }

    //Cancel dialog
    private void DialogCancel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to stop change?");
        //Confirm:
        builder.setNegativeButton("Yes",(dialogInterface, i) -> onBackPressed());
        //Stay:
        builder.setPositiveButton("No",(dialogInterface, i) -> {
            //Stay
        });
        builder.show();
    }

    //show Toast
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}