package tech.android.jobsharing.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Objects;

import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityJobDetailBinding;
import tech.android.jobsharing.models.Job;
import tech.android.jobsharing.models.User;
import tech.android.jobsharing.utils.TimeAgo;

public class JobDetailActivity extends BaseActivity {
    private ActivityJobDetailBinding binding;
    public static final String DATA_JOB = "data_job";
    private Job job;
    @Override
    protected void initAction() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.actJobDetailLlPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobDetailActivity.this,ProfileViewerActivity.class);
                intent.putExtra("userId",job.getUserId());
                startActivity(intent);
            }
        });

    }

    @Override
    protected void initData() {
        job = (Job) getIntent().getParcelableExtra(DATA_JOB);
        binding.actJobDetailTvCompany.setText(job.getCompany());
        binding.actJobDetailTvTitle.setText(job.getPosition());
        binding.actJobDetailTvLocation.setText(job.getLocation());
        binding.actJobDetailTvJobType.setText(job.getJobType());
        binding.actJobDetailTvWorkplace.setText(job.getWorkplaceType());
        binding.actJobDetailTvDescription.setText(job.getDescription());
        binding.actJobTvTime.setText((new TimeAgo()).covertTimeToText(job.getDateCreated()));
        if (!job.getImageCompany().isEmpty()){
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(job.getImageCompany(), binding.actJobDetailImvCompany);
        }
        getUserDetails(job.getUserId());
        if (job.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            binding.actJobDetailTvContact.setVisibility(View.GONE);
        binding.actJobDetailTvContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobDetailActivity.this, ChatActivity.class);
                intent.putExtra("userId",job.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }
    private void getUserDetails(String userId){
        if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final User user = snapshot.getValue(User.class);
                    if(user.getDescription() != null){
                        binding.actJobDetailTvUserDes.setText(user.getDescription());
                    }
                    if(user.getName() != null){
                        binding.actJobDetailTvUser.setText(user.getName());
                    }
                    if (user.getImage() != null) {
                        binding.actJobDetailImvAvata.setImageBitmap(getProfileImage(user.getImage()));
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
    @Override
    protected void bindView() {
        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}