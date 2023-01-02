package tech.android.jobsharing.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tech.android.jobsharing.R;
import tech.android.jobsharing.adapter.CommentListAdapter;
import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityCommentBinding;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;

public class CommentGroupActivity extends BaseActivity {

    private static final String TAG ="CommentActivity" ;
    Post post;
    ArrayList<Comments> mComments;
    Integer commentCount;
    String groupId;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private ActivityCommentBinding binding;
    @Override
    protected void initAction() {

    }

    @Override
    protected void initData() {
        try{
            post = getPhotoFromBundle();
            commentCount = getIntent().getIntExtra("commentcount",0);
            groupId = getIntent().getStringExtra("groupId");
            Log.d(TAG, "getPhotoFromBundle: arguments: " + post);

            getCommentList();

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage() );
        }
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final User user = snapshot.getValue(User.class);
                binding.actCommentImvAvatar.setImageBitmap(getProfileImage(user.getImage()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private Post getPhotoFromBundle() {

        Bundle bundle = getIntent().getExtras();
//        Log.d(TAG, "getPhotoFromBundle: arguments: " + bundle.getParcelable("Photo"));
        if(bundle != null) {
            return bundle.getParcelable("Post");
        }else{
            return null;
        }

    }
    @Override
    protected void bindView() {
        mComments = new ArrayList<>();
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backFromViewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.actPostTvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.actCommentEdtComment.getText().toString().isEmpty()){
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(binding.actCommentEdtComment.getText().toString());
                    binding.actCommentEdtComment.setText("");
                    closeKeyboard();
                }else{
                    Toast.makeText(CommentGroupActivity.this, "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void getCommentList(){
        Log.d(TAG, "getCommentList: Comments");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        if(commentCount == 0){
            mComments.clear();
            binding.actCommentLlEmpty.setVisibility(View.VISIBLE);
            binding.actCommentListView.setVisibility(View.GONE);
        }

        myRef.child("Post_Group")
                .child(groupId)
                .child(post.getUser_id())
                .child(post.getPhoto_id())
                .child("comments")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Log.d(TAG, "onChildAdded: child added.");

                        Query query = myRef
                                .child("Post_Group")
                                .child(groupId)
                                .child(post.getUser_id())
                                .orderByChild("post_id")
                                .equalTo(post.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dsnapshot) {
                                for ( DataSnapshot singleSnapshot :  dsnapshot.getChildren()){
                                    Post photo = new Post();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                    photo.setCaption(objectMap.get("caption").toString());
                                    photo.setTags(objectMap.get("tags").toString());
                                    photo.setPhoto_id(objectMap.get("post_id").toString());
                                    if (objectMap.get("userId") != null)
                                    photo.setUser_id(objectMap.get("userId").toString());
                                    photo.setDate_Created(objectMap.get("date_Created").toString());
                                    if (objectMap.get("image_Path") != null)
                                        photo.setImage_Path(objectMap.get("image_Path").toString());

                                    mComments.clear();
                                    Comments firstComment = new Comments();
                                    firstComment.setUser_id(post.getUser_id());
                                    firstComment.setComment(post.getCaption());
                                    firstComment.setDate_created(post.getDate_Created());

//                                    List<Comments> commentsList = new ArrayList<Comments>();
                                    for (DataSnapshot dSnapshot : singleSnapshot
                                            .child("comments").getChildren()){
                                        Comments comments = new Comments();
                                        comments.setUser_id(dSnapshot.getValue(Comments.class).getUser_id());
                                        comments.setComment(dSnapshot.getValue(Comments.class).getComment());
                                        comments.setDate_created(dSnapshot.getValue(Comments.class).getDate_created());
                                        mComments.add(comments);
                                    }
                                    photo.setComments(mComments);
                                    post =photo;
                                    setupWidgets();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setupWidgets(){

        CommentListAdapter adapter = new CommentListAdapter(this,R.layout.item_each_comment, mComments);
        binding.actCommentListView.setAdapter(adapter);

        if (mComments.size() > 0) {
            binding.actCommentLlEmpty.setVisibility(View.GONE);
            binding.actCommentListView.setVisibility(View.VISIBLE);
        }
    }
    private void addCommentNotification(String comment , String userid , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");

        HashMap<String, Object> hashMappp = new HashMap<>();
        hashMappp.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMappp.put("text", "Commented!"+comment);
        hashMappp.put("postid",postid);
        hashMappp.put("ispost", true);
        reference.child(userid).push().setValue(hashMappp);

    }
    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = myRef.push().getKey();

        Comments comment = new Comments();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into photos node

        //insert into user_photos node
        myRef.child("Post_Group")
                .child(groupId)
                .child(post.getUser_id())
                .child(post.getPhoto_id())
                .child("comments")
                .child(commentID)
                .setValue(comment);

        addCommentNotification(comment.getComment(), post.getUser_id(), post.getPhoto_id());

    }

}
