package tech.android.jobsharing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import tech.android.jobsharing.R;
import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityNewPostBinding;
import tech.android.jobsharing.models.User;
import tech.android.jobsharing.utils.Methods;

public class NewPostGroupActivity extends BaseActivity {
    private String encodeImage;
    private String userId;
    private String groupId;
    private ActivityNewPostBinding binding;
    private  Uri imageUri;
    DatabaseReference databaseReference,data;
    StorageReference storageReference,ref;
    String RandomUId;
    int PICK_IMAGE_REQUEST=1;
    Methods method;
    int count = 0;
    String postCount;
    public static final String DATA_GROUP_ID = "data_group_id";

    @Override
    protected void initAction() {
        binding.imageBack.setOnClickListener(v -> DialogCancel());
        binding.actNewPostFabImage.setOnClickListener(v -> openFileChooser());
        binding.actNewPostImvPost.setOnClickListener(v ->
                        upLoadImage()
                );
    }

    @Override
    protected void initData() {
        getUserDetails();
        groupId = getIntent().getStringExtra(DATA_GROUP_ID);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        method = new Methods();
        count = getCount();
    }

    @Override
    protected void bindView() {
        binding = ActivityNewPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
                    if (user.getName() != null) {
                        binding.actNewPostTvName.setText(user.getName());
                    }
                    if (user.getImage() != null) {
                        binding.actNewPostImvAvatar.setImageBitmap(getProfileImage(user.getImage()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            showToast("User id was not found!");
        }
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();
            binding.actNewPOstImvImage.setImageURI(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void DialogCancel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.do_you_want_to_remove));
        builder.setNegativeButton(getString(R.string.remove),(dialogInterface, i) ->
                setResultOk()
        );
        builder.setPositiveButton(getString(R.string.txt_continue),(dialogInterface, i) -> {
        });
        builder.show();
    }
    private void setResultOk(){
        setResult(Activity.RESULT_OK,new Intent());
        finish();
    }
    private void setResultCancel(){
        setResult(Activity.RESULT_CANCELED,new Intent());
        finish();
    }
    private void upLoadImage() {
        if (imageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(NewPostGroupActivity.this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String status = binding.actNewPostEdtStatus.getText().toString().trim();
            String tags = binding.actNewPostEdtTag.getText().toString().trim();
            RandomUId = UUID.randomUUID().toString();
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ref = storageReference.child("photos/users/"+"/"+userId+"/photo"+(count+1));
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            increasePostCount(count);
                            addPost(status, getTimestamp(), String.valueOf(uri), RandomUId, userId, tags);
                            progressDialog.dismiss();
                            showToast(getString(R.string.posted_successfully));
                            setResultOk();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewPostGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    setResultCancel();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    progressDialog.setCanceledOnTouchOutside(false);
                }
            });

        }else {
            if (!binding.actNewPostEdtStatus.getText().toString().isEmpty()){
                String status = binding.actNewPostEdtStatus.getText().toString().trim();
                String tags = binding.actNewPostEdtTag.getText().toString().trim();
                increasePostCount(count);
                RandomUId = UUID.randomUUID().toString();
                addPostNoImage(status, getTimestamp(), RandomUId, userId, tags);
                setResultOk();
            }else {
                showToast("Write something or add image");
            }
        }

    }

    //******************************FUNCTION TO ADD PHOTO TO FIREBASE STORAGE********
    public void addPost(String caption, String date_Created, String image_Path, String post_id, String user_id, String tags){
        HashMap<String, String> hashMappp = new HashMap<>();
        hashMappp.put("caption", caption);
        hashMappp.put("date_Created", date_Created);
        hashMappp.put("image_Path", image_Path);
        hashMappp.put("post_id", post_id);
        hashMappp.put("tags", tags);
        hashMappp.put("userId", user_id);
        databaseReference.child("Post_Group").child(groupId).child(user_id).child(post_id).setValue(hashMappp);
    }
    public void addPostNoImage(String caption, String date_Created,String post_id, String user_id, String tags){
        HashMap<String, String> hashMappp = new HashMap<>();
        hashMappp.put("caption", caption);
        hashMappp.put("date_Created", date_Created);
        hashMappp.put("post_id", post_id);
        hashMappp.put("tags", tags);
        hashMappp.put("userId", user_id);
        databaseReference.child("Post_Group").child(groupId).child(user_id).child(post_id).setValue(hashMappp);

    }


    //******************************FUNCTION TO INCREASE POST COUNT********
    public void increasePostCount(final int count){
        data = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postCount = Integer.toString(count+1);
                data.child("posts").setValue(postCount);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public int getCount() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count = method.getImageCount(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return count;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}