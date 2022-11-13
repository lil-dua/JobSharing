package tech.android.jobsharing.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import tech.android.jobsharing.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //set event listeners
        setListeners();
    }
    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignInActivity.class)));
        binding.buttonResetPassword.setOnClickListener(v -> {
           loading(true);
           final String email = binding.inputEmail.getText().toString();
           mAuth.sendPasswordResetEmail(email)
                   .addOnSuccessListener(unused -> {
                       loading(false);
                       showToast("Email sent!");
                   })
                   .addOnFailureListener(exception ->{
                       loading(false);
                      showToast(exception.getMessage());
                   });
        });

    }
    //show Toast
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // Check valid sign in
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }
        // if all information was valid and correct request.
        return true;
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonResetPassword.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.buttonResetPassword.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}