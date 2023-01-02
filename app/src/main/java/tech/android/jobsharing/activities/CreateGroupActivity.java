package tech.android.jobsharing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import tech.android.jobsharing.R;
import tech.android.jobsharing.databinding.ActivityCreateGroupBinding;
import tech.android.jobsharing.utils.LanguageConfig;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private StorageReference storageReference,ref;
    private Uri imageUri;
    private String userId;
    private String groupId;
    int count = 0;
    int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sharedPref = newBase.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String language = sharedPref.getString("language", "vi");
        Context context = LanguageConfig.changeLanguage(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        //setListener
        setListener();
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> {DialogCancel();});
        binding.imageDone.setOnClickListener(v -> {uploadData();});
        // Image Profile
        binding.imageBackground.setOnClickListener(v -> {
            openFileChooser();
        });
    }
    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //Cancel dialog
    private void DialogCancel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.do_you_want_to_stop));
        //Confirm:
        builder.setNegativeButton(getString(R.string.yes),(dialogInterface, i) -> onBackPressed());
        //Stay:
        builder.setPositiveButton(R.string.no,(dialogInterface, i) -> {
            //Stay
        });
        builder.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
        binding.textAddImage.setVisibility(View.GONE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();
            binding.imageBackground.setImageURI(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void setResultOk(){
        setResult(Activity.RESULT_OK,new Intent());
        finish();
    }
    private void setResultCancel(){
        setResult(Activity.RESULT_CANCELED,new Intent());
        finish();
    }

    private void uploadData(){
        if(imageUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(CreateGroupActivity.this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final String grName = binding.inputName.getText().toString().trim();
            final String grDescription = binding.inputDescription.getText().toString().trim();
            groupId = UUID.randomUUID().toString();
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ref = storageReference.child("groups/"+"/"+groupId+"/photo"+(count+1));
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                createGroup(grName,grDescription,String.valueOf(uri),groupId,userId,getTimestamp());
                                progressDialog.dismiss();
                                setResultOk();
                    })).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            showToast(e.getMessage());
                            setResultCancel();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            progressDialog.setCanceledOnTouchOutside(false);
                        }
                    });
        }else {
            showToast("No data result!");
        }
    }

    private void createGroup(String name, String description, String imagePath, String groupId,String userId,String dateCreated){
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("groupName",name);
        hashMap.put("groupDescription",description);
        hashMap.put("imagePath",imagePath);
        hashMap.put("groupId",groupId);
        hashMap.put("userId",userId);
        hashMap.put("dateCreated",dateCreated);
        hashMap.put("member","1");
        databaseReference.child("Member")
                .child(groupId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("user_id")
                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.child("Groups").child(userId).child(groupId).setValue(hashMap);
    }

    public String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}