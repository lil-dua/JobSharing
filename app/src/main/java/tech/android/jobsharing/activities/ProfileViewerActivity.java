package tech.android.jobsharing.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tech.android.jobsharing.adapter.JobsAdapter;
import tech.android.jobsharing.adapter.NewFeedAdapter;
import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityNewPostBinding;
import tech.android.jobsharing.databinding.ActivityProfileViewerBinding;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Job;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;

public class ProfileViewerActivity  extends BaseActivity {
    private ActivityProfileViewerBinding binding;
    private ArrayList<Post> mPost;
    private ArrayList<Job> jobs;
    private String userId;
    private NewFeedAdapter mAdapter;
    private JobsAdapter jobsAdapter;
    private int follower;
    @Override
    protected void initAction() {
        binding.imvBack.setOnClickListener(v-> onBackPressed());
        binding.fmProfileTvMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewerActivity.this, ChatActivity.class);
                intent.putExtra("userId",userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        binding.fmProfileTvMessage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewerActivity.this, ChatActivity.class);
                intent.putExtra("userId",userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        binding.fmProfileTvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Following")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(userId)
                        .child("user_id")
                        .setValue(userId);

                FirebaseDatabase.getInstance().getReference()
                        .child("Followers")
                        .child(userId)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("user_id")
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                binding.fmProfileLlViewer.setVisibility(View.GONE);
                int followUpdate = follower+1;
                binding.numFollower.setText("" +followUpdate);
                binding.fmProfileTvMessage2.setVisibility(View.VISIBLE);
                increaseFollowers();
                increaseFollowing();
                addFollowNotification();
            }
        });
    }
    @Override
    protected void initData() {
    }
    @Override
    protected void bindView() {
        binding = ActivityProfileViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userId = getIntent().getStringExtra("userId");
        mPost = new ArrayList<>();
        jobs = new ArrayList<>();
        getUserDetails();
        getJobList();
        getPostList();
        isFollowing(userId);
    }
    public void getUserDetails() {
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final User user = snapshot.getValue(User.class);
                    Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();
                    binding.txtDescription.setText(user.getDescription());
                    if (user.getLink().isEmpty()){
                        binding.txtLink.setVisibility(View.GONE);
                    } else {
                        binding.txtLink.setText(user.getLink());
                    }
                    //check null name
                    if (user.getName() != null) {
                        binding.txtUserName.setText(user.getName());
                    }
                    //check null image
                    if (user.getImage() != null) {
                        binding.imageProfile.setImageBitmap(getProfileImage(user.getImage()));
                    }

                    //text link
                    binding. txtLink.setOnClickListener(v -> {
                        Intent intent = new Intent(ProfileViewerActivity.this, WebViewActivity.class);
                        intent.putExtra("link", "https://" + user.getLink());
                        startActivity(intent);
                    });
                    if (objectMap.get("followers") != null) {
                        binding.numFollower.setText(objectMap.get("followers").toString());
                        follower = Integer.valueOf(objectMap.get("followers").toString());
                    }
                    ;
                    if (objectMap.get("following") != null) {
                        binding. numFollower.setText(objectMap.get("following").toString());
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            showToast("User id was not found!");
        }
    }
    private void getPostList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Post")
                .child(userId)
                .orderByChild("userId")
                .equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    binding.fmProfileCtNewsfeed.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Post photo = new Post();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    photo.setCaption(objectMap.get("caption").toString());
                    photo.setTags(objectMap.get("tags").toString());
                    if (objectMap.get("post_id") != null)
                        photo.setPhoto_id(objectMap.get("post_id").toString());
                    if (objectMap.get("userId") != null)
                        photo.setUser_id(objectMap.get("userId").toString());
                    photo.setDate_Created(objectMap.get("date_Created").toString());
                    if (objectMap.get("image_Path") != null)
                        photo.setImage_Path(objectMap.get("image_Path").toString());
                    ArrayList<Comments> comments = new ArrayList<Comments>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child("comments").getChildren()) {
                        Comments comment = new Comments();
                        comment.setUser_id(dSnapshot.getValue(Comments.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comments.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comments.class).getDate_created());
                        comments.add(comment);
                    }
                    photo.setComments(comments);
                    mPost.add(photo);
                }
                //display our photos
                displayPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayPhotos() {
        if (mPost != null) {
            try {
                Collections.sort(mPost, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o2.getDate_Created().compareTo(o1.getDate_Created());
                    }
                });
                mAdapter = new NewFeedAdapter(ProfileViewerActivity.this, mPost);
                mAdapter.setGoneFollow(true);
                binding.newsfeedRecyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.frameNoPost.setVisibility(View.GONE);
                binding.newsfeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                binding.newsfeedRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            } catch (NullPointerException e) {
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
            }
        }
    }

    private void getJobList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Job")
                .child(userId)
                .orderByChild("userId")
                .equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    binding.fmProfileRcvJob.setVisibility(View.GONE);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Job job = singleSnapshot.getValue(Job.class);
                    jobs.add(job);
                }
                //display our photos
                displayJob();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void displayJob(){
        if(jobs != null){
            try{
                Collections.sort(jobs, new Comparator<Job>() {
                    @Override
                    public int compare(Job o1, Job o2) {
                        return o2.getDateCreated().compareTo(o1.getDateCreated());
                    }
                });

                jobsAdapter = new JobsAdapter(ProfileViewerActivity.this, jobs, new JobsAdapter.OnActionListener() {
                    @Override
                    public void onClick(Job job) {
                        Intent intent = new Intent(ProfileViewerActivity.this, JobDetailActivity.class);
                        intent.putExtra(JobDetailActivity.DATA_JOB,job);
                        startActivity(intent);
                    }
                });
                binding.fmProfileRcvJob.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.frameNoPost.setVisibility(View.GONE);
                binding.fmProfileRcvJob.setLayoutManager(new LinearLayoutManager(this));
                binding.fmProfileRcvJob.setAdapter(jobsAdapter);
                jobsAdapter.notifyDataSetChanged();
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }
    private void isFollowing( String userId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("user_id").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());
                    binding.fmProfileLlViewer.setVisibility(View.GONE);
                    binding.fmProfileTvMessage2.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void increaseFollowing(){
        Log.d(TAG, "increaseFollowing: Increasing Following Count");

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("following").getValue() != null){
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("following").getValue().toString()) + 1);
                    data.child("following").setValue(postCount);
                }else {
                    data.child("following").setValue("1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public void decreaseFollowing(){
        Log.d(TAG, "decreaseFollowing: decreasing Following Count");

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("following").getValue() !=null) {
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("following").getValue().toString()) - 1);
                    data.child("following").setValue(postCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void decreaseFollowers(Post post){
        Log.d(TAG, "decreaseFollowers: decreasing Followers Count");

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users")
                .child(post.getUser_id());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("followers").getValue() != null){
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("followers").getValue().toString()) - 1);
                    data.child("followers").setValue(postCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void increaseFollowers(){
        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId);
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("followers").getValue() != null) {
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("followers").getValue().toString()) + 1);
                    data.child("followers").setValue(postCount);
                }else
                    data.child("followers").setValue("1");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
    private void addFollowNotification(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");

        HashMap<String, Object> hashMappp = new HashMap<>();
        hashMappp.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMappp.put("text", "started following you");
        hashMappp.put("postid", "");
        hashMappp.put("ispost", false);
        reference.child(userId).push().setValue(hashMappp);

    }

}
