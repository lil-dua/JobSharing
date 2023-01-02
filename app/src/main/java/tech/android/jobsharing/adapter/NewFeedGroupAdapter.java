package tech.android.jobsharing.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.CommentGroupActivity;
import tech.android.jobsharing.activities.ProfileViewerActivity;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Likes;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;
import tech.android.jobsharing.utils.TimeAgo;

public class NewFeedGroupAdapter extends RecyclerView.Adapter<NewFeedGroupAdapter.ViewHolder>{
    public NewFeedGroupAdapter(@NonNull Context context, @NonNull List<Post> objects, String groupId) {
        this.mContext = context;
        this.objects = objects;
        this.groupId = groupId;
        mReference = FirebaseDatabase.getInstance().getReference();
        currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private String TAG = "NewFeedGroupAdapter";
    private Boolean isGoneFollow = false;
    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    public void setGoneFollow(Boolean b){
        this.isGoneFollow = b;
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private String currentId = "";
    private String groupId = "";
    private ProgressBar mProgressBar;
    private List<Post> objects;



    @NonNull
    @Override
    public NewFeedGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_feed,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewFeedGroupAdapter.ViewHolder holder, int position) {
        Post post = objects.get(position);
        if (position == 0)
            holder.line.setVisibility(View.GONE);
        else
            holder.line.setVisibility(View.VISIBLE);
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);
        holder.photo = post;
        holder.detector = new GestureDetector( holder.itemView.getContext(), new GestureListener(holder));
        holder.users = new StringBuilder();
        if (currentId.equals(post.getUser_id())){
            holder.follow.setVisibility(View.GONE);
            holder.unFollow.setVisibility(View.GONE);
        }
        getCurrentUsername();
        getLikesString(holder);

        if (post.getTags() != null && !post.getTags().isEmpty()){
            holder.mTags.setText(post.getTags());
        } else {
            holder.mTags.setVisibility(View.GONE);
        }
        if (post.getCaption() != null && !post.getCaption().isEmpty()){
            holder.caption.setText(post.getCaption());
        } else {
            holder.caption.setVisibility(View.GONE);
        }
        final List<Comments> comments = post.getComments();
        if (comments.size() != 0){
            Log.d("Xuannv", "check: "+post.getCaption() +" comment"+ comments.size());
            holder.comments.setVisibility(View.VISIBLE);
            holder.comments.setText(String.format(mContext.getString(R.string.format_comments),String.valueOf(comments.size())));
        }else {
            holder.comments.setVisibility(View.GONE);
        }
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(mContext, CommentGroupActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Post", post);
                b.putExtra("commentcount",comments.size());
                b.putExtra("groupId",groupId);
                b.putExtras(bundle);
                mContext.startActivity(b);

            }
        });
        holder.timeDetla.setText((new TimeAgo(mContext)).covertTimeToText(post.getDate_Created()));
        final ImageLoader imageLoader = ImageLoader.getInstance();
        if (post.getImage_Path() != null)
            imageLoader.displayImage(post.getImage_Path(), holder.image);
        else
            holder.image.setVisibility(View.GONE);
        if (!currentId.equals(post.getUser_id())) {
            isFollowing(holder, post.getUser_id());
        }
        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Users")
                .orderByChild("userId")
                .equalTo(post.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    // currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                    Log.d(TAG, "onDataChange: found user: "
                            + singleSnapshot.getValue(User.class).getName());

                    holder.username.setText(singleSnapshot.getValue(User.class).getName());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileViewerActivity.class);
                            intent.putExtra("userId",post.getUser_id());
                            mContext.startActivity(intent);
                        }
                    });
                    holder.avatar.setImageBitmap(getProfileImage(singleSnapshot.getValue(User.class).getImage()));
                    holder.avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileViewerActivity.class);
                            intent.putExtra("userId",post.getUser_id());
                            mContext.startActivity(intent);
                        }
                    });

                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent b = new Intent(mContext, CommentGroupActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("Post", post);
                            b.putExtra("commentcount",comments.size());
                            b.putExtra("groupId",groupId);
                            b.putExtras(bundle);
                            mContext.startActivity(b);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Following")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(post.getUser_id())
                        .child("user_id")
                        .setValue((post.getUser_id()));

                FirebaseDatabase.getInstance().getReference()
                        .child("Followers")
                        .child(post.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("user_id")
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing(holder);
                increaseFollowers(post);
                increaseFollowing();
                addFollowNotification(post.getUser_id());
            }
        });

        holder.unFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference()
                        .child("Following")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(post.getUser_id())
                        .removeValue();
                FirebaseDatabase.getInstance().getReference()
                        .child("Followers")
                        .child(post.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing(holder);
                decreaseFollowers(post);
                decreaseFollowing();
            }
        });
        if(reachedEndOfList(position)){
            loadMoreData();
        }
    }
    public Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }


    @Override
    public int getItemCount() {
        return objects.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView avatar;
        String likesString = "";
        TextView username, timeDetla, caption, likes, comments,mTags,follow,unFollow;
        RoundedImageView image;
        ImageView heartRed, heartWhite, comment;
        User settings = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Post photo;
        View line;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.itNewFeed_tvName);
            line = (View) itemView.findViewById(R.id.itNewFeed_line);
            image = (RoundedImageView) itemView.findViewById(R.id.itNewFeed_imvPostImage);
            heartRed = (ImageView) itemView.findViewById(R.id.itNewFeed_imvHeartRed);
            heartWhite = (ImageView) itemView.findViewById(R.id.itNewFeed_imvHeart);
            comment = (ImageView) itemView.findViewById(R.id.itNewFeed_imvComment);
            likes = (TextView) itemView.findViewById(R.id.itNewFeed_tvLike);
            comments = (TextView) itemView.findViewById(R.id.itNewFeed_tvUserComment);
            caption = (TextView) itemView.findViewById(R.id.itNewFeed_tvStatus);
            timeDetla = (TextView) itemView.findViewById(R.id.itNewFeed_tvTimePost);
            avatar = (RoundedImageView) itemView.findViewById(R.id.itNewFeed_imvAvatar);
            mTags = (TextView)itemView.findViewById(R.id.itNewFeed_tvTag);
            follow = (TextView)itemView.findViewById(R.id.itNewFeed_tvFollow);
            unFollow = (TextView)itemView.findViewById(R.id.itNewFeed_tvUnFollow);

        }
    }
    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("Post_Group")
                    .child(groupId)
                    .child(mHolder.photo.getUser_id())
                    .child(mHolder.photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String keyID = singleSnapshot.getKey();
                        if(mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Likes.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            mReference.child("Post_Group")
                                    .child(groupId)
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        else if(!mHolder.likeByCurrentUser){
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }
    private void addNewLike(final ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");
        String newLikeID = mReference.push().getKey();
        Likes like = new Likes();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mReference.child("Post")
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child("likes")
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
        addLikeNotification(holder.photo.getUser_id(),holder.photo.getPhoto_id());

    }
    private void getCurrentUsername(){
        Log.d(TAG, "getCurrentUsername: retrieving users");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Users")
                .orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    currentUsername = singleSnapshot.getValue(User.class).getName();
                }
//                getLikesString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getCurrentUsername: query cancelled.");

            }
        });
    }
    private void getLikesString(final ViewHolder holder){
        try{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("Post_Group")
                    .child(groupId)
                    .child(holder.photo.getUser_id())
                    .child(holder.photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child("Users")
                                .orderByChild("userId")
                                .equalTo(singleSnapshot.getValue(Likes.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                    Log.d(TAG, "onDataChange: found like: " +
                                            singleSnapshot.getValue(User.class).getName());

                                    holder.users.append(singleSnapshot.getValue(User.class).getName());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");
                                Log.d(TAG, "onDataChange: getLikesString: holder.users.toString():" +
                                        holder.users.toString());
                                Log.d(TAG, "onDataChange: getLikesString: currentUsername:" +
                                        currentUsername);


                                if(holder.users.toString().contains(currentUsername + ",")){
                                    holder.likeByCurrentUser = true;
                                }else{
                                    holder.likeByCurrentUser = false;
                                }

                                int length = splitUsers.length;
                                if(length == 1){
                                    holder.likesString = "Liked by " + splitUsers[0];
                                }
                                else if(length == 2){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + " and " + splitUsers[1];
                                }
                                else if(length == 3){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + " and " + splitUsers[2];

                                }
                                else if(length == 4){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                }
                                else if(length > 4){
                                    holder.likesString = "Liked by " + splitUsers[0]
                                            + ", " + splitUsers[1]
                                            + ", " + splitUsers[2]
                                            + " and " + (splitUsers.length - 3) + " others";
                                }
                                Log.d(TAG, "onDataChange: likes string: " + holder.likesString);
                                //setup likes string
                                setupLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    if(!dataSnapshot.exists()){
                        holder.likesString = "";
                        holder.likeByCurrentUser = false;
                        //setup likes string
                        setupLikesString(holder, holder.likesString);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage() );
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            //setup likes string
            setupLikesString(holder, holder.likesString);
        }
    }
    private void setupLikesString(final ViewHolder holder, String likesString){
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);
        if(holder.likeByCurrentUser){
            Log.d(TAG, "setupLikesString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }else{
            Log.d(TAG, "setupLikesString: photo is not liked by current user");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
//        mProgressBar.setVisibility(View.GONE);

    }
    private boolean reachedEndOfList(int position){
        return position == getItemCount() - 1;
    }
    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) this;
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }
    private void addLikeNotification(String userid,String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications");
        HashMap<String, Object> hashMappp = new HashMap<>();
        hashMappp.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMappp.put("postid", postid);
        hashMappp.put("text", "liked your post");
        hashMappp.put("ispost", true);
        reference.child(userid).push().setValue(hashMappp);

    }
    private void isFollowing(final ViewHolder holder, String userId){
        setUnfollowing(holder);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("Following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild("user_id").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());
                    setFollowing(holder);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void setFollowing(final ViewHolder holder){
        holder.follow.setVisibility(View.GONE);
        holder.unFollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing(final ViewHolder holder){
        holder.follow.setVisibility(View.VISIBLE);
        holder.unFollow.setVisibility(View.GONE);
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
    public void increaseFollowers(Post post){
        final DatabaseReference data = FirebaseDatabase.getInstance().getReference("Users")
                .child(post.getUser_id());
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
