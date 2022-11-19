package tech.android.jobsharing.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import tech.android.jobsharing.databinding.ActivitySignUpBinding;
import tech.android.jobsharing.models.User;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private String encodeImage;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set binding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        //set event listeners
        setListeners();
    }
    private void setListeners() {
        // switching to Sign In Activity
        binding.textSignIn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignInActivity.class)));
        // Access to Main Activity
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        // Image Profile
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void signUp(){
        loading(true);
        //Get data from edit text into String variables
        final String name = binding.inputName.getText().toString().trim();
        final String email = binding.inputEmail.getText().toString().trim();
        final String studentId = binding.inputStudentID.getText().toString().trim();
        final String description = binding.inputDescription.getText().toString().trim();
        final String link = binding.inputLink.getText().toString().trim();
        final String image = encodeImage;
        final String password = binding.inputPassword.getText().toString().trim();
        //Create user
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(authResult -> {
                    loading(false);
                    String userId = mAuth.getCurrentUser().getUid();
                    User user = new User(name,email,studentId,description,link,image,password,userId);
                    //put user to Realtime database
                    databaseReference.child("Users").child(userId).setValue(user)
                            .addOnSuccessListener(unused -> {
                                //Access to MainActivity
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(exception ->{
                                loading(false);
                                showToast(exception.getMessage());
                            });
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
                });



    }

    //show Toast
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // this function will encode an image you choose in gallery.
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Pick image function
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    // Check valid sign up
    private Boolean isValidSignUpDetails(){
        if(encodeImage == null){
            showToast("Select your image");
            return false;
        }else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            binding.inputName.setError("User name is required!");
            binding.inputName.requestFocus();
            return false;
        }else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(binding.inputStudentID.getText().toString().trim().isEmpty()){
            showToast("Enter student ID");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm password");
            return false;
        }else if(!binding.inputPassword.getText().toString()
                .equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password and Confirm password must be same");
            return false;
        }
        // if all information was valid and correct request.
        return true;
    }

    // loading for Sign Up
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}