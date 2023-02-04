package tech.android.jobsharing.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.EditProfileActivity;
import tech.android.jobsharing.activities.JobDetailActivity;
import tech.android.jobsharing.activities.SettingsActivity;
import tech.android.jobsharing.activities.WebViewActivity;
import tech.android.jobsharing.adapter.JobsAdapter;
import tech.android.jobsharing.adapter.NewFeedAdapter;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Job;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;


/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Profile extends Fragment {
    private static final String TAG = "Fragment_Profile";
    View view;
    RoundedImageView imageProfile, imageEditProfile;
    TextView txtName, txtDescription, txtLink, numPost, numFollower, numFollowing;
    AppCompatImageView imgSetting;
    FrameLayout frameNoPost;
    ProgressBar progressBar;
    private RecyclerView newsfeedRcv, jobRcv;
    private ConstraintLayout fmProfile_ctNewsfeed, fmProfile_ctJob;
    private ArrayList<Post> mPost;
    private ArrayList<Job> jobs;

    private NewFeedAdapter mAdapter;
    private JobsAdapter jobsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        //Init
        init();
        getUserDetails();
        getPostList();
        getJobList();
        //set event listeners
        if (jobs.isEmpty() && mPost.isEmpty()){
            frameNoPost.setVisibility(View.VISIBLE);
        }
        setListeners();
        return view;
    }

    private void init() {
        imgSetting = view.findViewById(R.id.imageSetting);
        imageProfile = view.findViewById(R.id.imageProfile);
        imageEditProfile = view.findViewById(R.id.imageEditProfile);
        txtName = view.findViewById(R.id.txtUserName);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtLink = view.findViewById(R.id.txtLink);
//        numPost =  view.findViewById(R.id.numPost);
        numFollower = view.findViewById(R.id.numFollower);
        numFollowing = view.findViewById(R.id.numFollowing);
        frameNoPost = view.findViewById(R.id.frameNoPost);
        progressBar = view.findViewById(R.id.progressBar);
        newsfeedRcv = view.findViewById(R.id.newsfeedRecyclerView);
        jobRcv = view.findViewById(R.id.fmProfile_rcvJob);
        fmProfile_ctNewsfeed = view.findViewById(R.id.fmProfile_ctNewsfeed);
        fmProfile_ctJob = view.findViewById(R.id.fmProfile_ctJob);
        progressBar.setVisibility(View.VISIBLE);
        mPost = new ArrayList<>();
        jobs = new ArrayList<>();
    }

    private void setListeners() {
        //Setting
        imgSetting.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        //Edit profile
        imageEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

    }

    public void getUserDetails() {
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final User user = snapshot.getValue(User.class);
                    Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();
                    txtDescription.setText(user.getDescription());
                    if (user.getLink().isEmpty()){
                        txtLink.setVisibility(View.GONE);
                    } else {
                        txtLink.setText(user.getLink());
                    }
                    //check null name
                    if (user.getName() != null)
                        txtName.setText(user.getName());
                    //check null image
                    if (user.getImage() != null)
                        imageProfile.setImageBitmap(getProfileImage(user.getImage()));
                    //text link
                    txtLink.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra("link", "https://" + user.getLink());
                        startActivity(intent);
                    });
                    if (objectMap.get("followers") != null)
                        numFollower.setText(objectMap.get("followers").toString());
                    if (objectMap.get("following") != null)
                        numFollower.setText(objectMap.get("following").toString());
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = reference
                .child("Post")
                .child(userId)
                .orderByChild("userId")
                .equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    fmProfile_ctNewsfeed.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
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
                mAdapter = new NewFeedAdapter(getActivity(), mPost);
                newsfeedRcv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                frameNoPost.setVisibility(View.GONE);
                newsfeedRcv.setLayoutManager(new LinearLayoutManager(getContext()));
                newsfeedRcv.setAdapter(mAdapter);
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = reference
                .child("Job")
                .child(userId)
                .orderByChild("userId")
                .equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    fmProfile_ctJob.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
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

                jobsAdapter = new JobsAdapter(getActivity(), jobs, new JobsAdapter.OnActionListener() {
                    @Override
                    public void onClick(Job job) {
                        Intent intent = new Intent(getActivity(), JobDetailActivity.class);
                        intent.putExtra(JobDetailActivity.DATA_JOB,job);
                        startActivity(intent);
                    }
                });
                jobRcv.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                frameNoPost.setVisibility(View.GONE);
                jobRcv.setLayoutManager(new LinearLayoutManager(getContext()));
                jobRcv.setAdapter(jobsAdapter);
                jobsAdapter.notifyDataSetChanged();
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    private Bitmap getProfileImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //show Toast
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


}
