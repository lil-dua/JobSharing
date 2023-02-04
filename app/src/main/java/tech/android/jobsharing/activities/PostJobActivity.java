package tech.android.jobsharing.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

import tech.android.jobsharing.R;
import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityPostJobBinding;
import tech.android.jobsharing.dialog.ExperienceDialog;
import tech.android.jobsharing.dialog.WorkplaceDialog;
import tech.android.jobsharing.models.Job;

public class PostJobActivity extends BaseActivity {
    private ActivityPostJobBinding binding;
    private String workplaceType = "";
    private String experience = "";
    private String userId;
    private int PICK_IMAGE_REQUEST = 123;
    DatabaseReference databaseReference, data;
    private Uri imageUri;
    StorageReference storageReference,ref;

    @Override
    protected void initAction() {
        workplaceType = getString(R.string.on_site);
        binding.actPostJobTvWorkplace.setOnClickListener(v -> {
            WorkplaceDialog dialog = WorkplaceDialog.newInstance(workplaceType, type -> {
                workplaceType = type;
                binding.actPostJobTvWorkplace.setText(type);
            });
            dialog.show(getSupportFragmentManager(), WorkplaceDialog.class.getName());
        });
        //experience
        experience = getString(R.string.none_experience);
        binding.actPostJobLlExperience.setOnClickListener(v -> {
            ExperienceDialog dialog = ExperienceDialog.newInstance(experience, type -> {
                experience = type;
                binding.actPostJobTvExperience.setText(type);
            });
            dialog.show(getSupportFragmentManager(),ExperienceDialog.class.getName());
        });
        //post job
        binding.actPostJobImvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.actPostJobEdtTitle.getText().toString().isEmpty()
                        &&!binding.actPostJobEdtCompany.getText().toString().isEmpty()
                        &&!binding.actPostJobTvWorkplace.getText().toString().isEmpty()
                        &&!binding.actPostJobTvLocation.getText().toString().isEmpty()
                        &&!binding.actPostJobTvDescription.getText().toString().isEmpty()
                ) {
                    upLoadImage();

                } else {
                    showToast(getString(R.string.please_fill));
                    setResultCancel();
                }
            }
        });
        binding.actPostJobImvCompany.setOnClickListener(v -> openFileChooser());
        binding.imageBack.setOnClickListener(v -> DialogCancel());

    }

    @Override
    protected void initData() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

    }

    @Override
    protected void bindView() {
        binding = ActivityPostJobBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    public void addJob(Job job) {
        HashMap<String, String> hashMappp = new HashMap<>();
        hashMappp.put("position", job.getPosition());
        hashMappp.put("company", job.getCompany());
        if (job.getImageCompany().isEmpty())
            hashMappp.put("imageCompany", "");
        else
            hashMappp.put("imageCompany", job.getImageCompany());
        hashMappp.put("workplaceType", job.getWorkplaceType());
        hashMappp.put("location", job.getLocation());
        hashMappp.put("jobType", job.getJobType());
        hashMappp.put("numberEmployee", job.getNumberEmployee());
        hashMappp.put("experience", job.getExperience());
        hashMappp.put("description", job.getDescription());
        hashMappp.put("requirement",job.getRequirement());
        hashMappp.put("benefit",job.getBenefit());
        hashMappp.put("jobId", job.getJobId());
        hashMappp.put("userId", job.getUserId());
        hashMappp.put("dateCreated", job.getDateCreated());
        databaseReference.child("Job").child(job.getUserId()).child(job.getJobId()).setValue(hashMappp);
    }
    private void setResultOk(){
        setResult(Activity.RESULT_OK,new Intent());
        finish();
    }
    private void setResultCancel(){
        setResult(Activity.RESULT_CANCELED,new Intent());
        finish();
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
            binding.actPostJobImvCompany.setImageURI(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void upLoadImage() {
        if (imageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(PostJobActivity.this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String RandomUId = UUID.randomUUID().toString();
            ref = storageReference.child("company/users/"+"/"+userId+"/photo"+RandomUId);
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Job job = new Job(
                        binding.actPostJobEdtTitle.getText().toString(),
                        binding.actPostJobEdtCompany.getText().toString(),
                        String.valueOf(uri),
                        binding.actPostJobTvWorkplace.getText().toString(),
                        binding.actPostJobTvLocation.getText().toString(),
                        binding.actPostJobTvType.getText().toString(),
                        binding.actPostJobTvNumberEmployee.getText().toString(),
                        binding.actPostJobTvExperience.getText().toString(),
                        binding.actPostJobTvDescription.getText().toString(),
                        binding.actPostJobTvRequirement.getText().toString(),
                        binding.actPostJobTvBenefit.getText().toString(),
                        RandomUId,
                        userId,
                        getTimestamp()
                    );
                    addJob(job);
                    progressDialog.dismiss();
                    showToast(getString(R.string.posted_successfully));
                    setResultOk();
                })).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PostJobActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    setResultCancel();
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    progressDialog.setCanceledOnTouchOutside(false);
                });
        }else {
            String RandomUId = UUID.randomUUID().toString();
            Job job = new Job(
                    binding.actPostJobEdtTitle.getText().toString(),
                    binding.actPostJobEdtCompany.getText().toString(),
                    "",
                    binding.actPostJobTvWorkplace.getText().toString(),
                    binding.actPostJobTvLocation.getText().toString(),
                    binding.actPostJobTvType.getText().toString(),
                    binding.actPostJobTvNumberEmployee.getText().toString(),
                    binding.actPostJobTvExperience.getText().toString(),
                    binding.actPostJobTvDescription.getText().toString(),
                    binding.actPostJobTvRequirement.getText().toString(),
                    binding.actPostJobTvBenefit.getText().toString(),
                    RandomUId,
                    userId,
                    getTimestamp()
            );
            addJob(job);
            showToast(getString(R.string.posted_successfully));
            setResultOk();
        }

    }

    private void DialogCancel(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.do_you_want_to_remove));
        builder.setNegativeButton(getString(R.string.remove),(dialogInterface, i) ->
                setResultCancel()
        );
        builder.setPositiveButton(getString(R.string.txt_continue),(dialogInterface, i) -> {
        });
        builder.show();
    }
}