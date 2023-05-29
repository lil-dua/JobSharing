package tech.android.jobsharing.activities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
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

import tech.android.jobsharing.adapter.NewFeedGroupAdapter;
import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityGroupsBinding;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Group;
import tech.android.jobsharing.models.Post;

public class GroupsActivity extends BaseActivity {
    private static final String TAG = "GroupsActivity";
    private ActivityGroupsBinding binding;
    public static final String DATA_GROUP = "data_group";
    private Group group;
    int LAUNCH_SECOND_ACTIVITY = 1;
    private ArrayList<Post> mPaginatedPhotos;
    private ArrayList<Post> mPost;
    private ArrayList<String> mFollowing;
    private int mResults;
    private NewFeedGroupAdapter mAdapter;

    @Override
    protected void initAction() {
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    protected void initData() {
        group = getIntent().getParcelableExtra(DATA_GROUP);
        binding.textTitle.setText(group.getGroupName());
        binding.textGroupName.setText(group.getGroupName());
        binding.textGroupDateCreated.setText(group.getDateCreated());
        if (group.getMember() != null)
            binding.textNumberMember.setText(group.getMember().toString());
            binding.textGroupDescription.setText(group.getGroupDescription());
        //load group background
        if (!group.getImagePath().isEmpty()){
            Glide.with(this).load(group.getImagePath()).into(binding.imageBackground);
        }
        getFollowing();
        isMember();
        binding.fabNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupsActivity.this, NewPostGroupActivity.class);
                intent.putExtra(NewPostGroupActivity.DATA_GROUP_ID, group.getGroupId());
                startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
            }
        });
        binding.btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });
        binding.btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGroup();
            }
        });
    }

    @Override
    protected void bindView() {
        binding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                getFollowing();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }
    private void getFollowing(){
        mPost = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();
        mFollowing = new ArrayList<>();
        binding.actGroupProgress.setVisibility(View.VISIBLE);
        binding.actGroupRcv.setVisibility(View.GONE);
        binding.actGroupLlEmpty.setVisibility(View.GONE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Post_Group")
                .child(group.getGroupId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    DataSnapshot dataSnapshot1 = singleSnapshot.getChildren().iterator().next();
                    mFollowing.add(dataSnapshot1.child("userId").getValue().toString());
                }
                if (mFollowing.isEmpty()){
                    binding.actGroupRcv.setVisibility(View.GONE);
                    binding.actGroupProgress.setVisibility(View.GONE);
                    binding.actGroupLlEmpty.setVisibility(View.VISIBLE);
                    return;
                }
                getPostList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostList(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++){
            final int count = i;
            Query query = reference
                    .child("Post_Group")
                    .child(group.getGroupId())
                    .child(mFollowing.get(i))
                    .orderByChild("userId")
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        binding.actGroupRcv.setVisibility(View.GONE);
                        binding.actGroupProgress.setVisibility(View.GONE);
                        binding.actGroupLlEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
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
                                .child("comments").getChildren()){
                            Comments comment = new Comments();
                            comment.setUser_id(dSnapshot.getValue(Comments.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comments.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comments.class).getDate_created());
                            comments.add(comment);
                        }
                        photo.setComments(comments);
                        mPost.add(photo);
                    }
                    if(count >= mFollowing.size() -1){
                        //display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    binding.actGroupRcv.setVisibility(View.GONE);
                    binding.actGroupProgress.setVisibility(View.GONE);
                    binding.actGroupLlEmpty.setVisibility(View.VISIBLE);
                }
            });
        }
    }
    private void displayPhotos(){
        if(mPost != null){
            try{
                Collections.sort(mPost, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o2.getDate_Created().compareTo(o1.getDate_Created());
                    }
                });
                int iterations = mPost.size();

                if(iterations > 10){
                    iterations = 10;
                }
                mResults = 10;
                for(int i = 0; i < iterations; i++){
                    mPaginatedPhotos.add(mPost.get(i));
                }
                mAdapter = new NewFeedGroupAdapter(GroupsActivity.this, mPaginatedPhotos,group.getGroupId());
                binding.actGroupRcv.setVisibility(View.VISIBLE);
                binding.actGroupProgress.setVisibility(View.GONE);
                binding.actGroupLlEmpty.setVisibility(View.GONE);
                binding.actGroupRcv.setLayoutManager(new LinearLayoutManager(this));
                binding.actGroupRcv.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }
    private void isMember(){
        String currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Member")
                .child(group.getGroupId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    DataSnapshot dataSnapshot1 = singleSnapshot.getChildren().iterator().next();
                    String userId = dataSnapshot1.getValue().toString();
                    if (userId.equals(currentId)){
                        binding.btnJoin.setVisibility(View.GONE);
                        binding.btnLeave.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void joinGroup(){
        FirebaseDatabase.getInstance().getReference()
                .child("Member")
                .child(group.getGroupId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("user_id")
                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Groups")
                .child(group.getUserId()).child(group.getGroupId());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("member").getValue() != null) {
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("member").getValue().toString()) + 1);
                    data.child("member").setValue(postCount);
                    binding.textNumberMember.setText(postCount);
                }
                binding.btnJoin.setVisibility(View.GONE);
                binding.btnLeave.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void leaveGroup(){
        FirebaseDatabase.getInstance().getReference()
                .child("Member")
                .child(group.getGroupId())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .removeValue();
        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Groups")
                .child(group.getUserId()).child(group.getGroupId());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("member").getValue() != null){
                    String postCount = Integer.toString(Integer.parseInt(snapshot.child("member").getValue().toString()) - 1);
                    data.child("member").setValue(postCount);
                    binding.textNumberMember.setText(postCount);
                }
                binding.btnJoin.setVisibility(View.VISIBLE);
                binding.btnLeave.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addFollowNotification(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        HashMap<String, Object> hashMappp = new HashMap<>();
        hashMappp.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMappp.put("text", "started following you");
        hashMappp.put("postid", "");
        hashMappp.put("ispost", false);
        reference.child(userid).push().setValue(hashMappp);

    }
}