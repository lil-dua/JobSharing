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
import tech.android.jobsharing.activities.CommentActivity;
import tech.android.jobsharing.models.Comments;
import tech.android.jobsharing.models.Likes;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;

public class NewFeedAdapter extends RecyclerView.Adapter<NewFeedAdapter.ViewHolder>{
    public NewFeedAdapter(@NonNull Context context, @NonNull List<Post> objects) {
        this.mContext = context;
        this.objects = objects;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    private String TAG = "NewFeedAdapter";
    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private ProgressBar mProgressBar;
    private List<Post> objects;



    @NonNull
    @Override
    public NewFeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_feed,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewFeedAdapter.ViewHolder holder, int position) {
        Post photo = objects.get(position);
        if (position == 0)
            holder.line.setVisibility(View.GONE);
        holder.heart = new Heart(holder.heartWhite, holder.heartRed);
        holder.photo = photo;
        holder.detector = new GestureDetector( holder.itemView.getContext(), new GestureListener(holder));
        holder.users = new StringBuilder();
        getCurrentUsername();
        getLikesString(holder);
        if (photo.getTags() != null && !photo.getTags().isEmpty()){
            holder.mTags.setText(photo.getTags());
        } else {
            holder.mTags.setVisibility(View.GONE);
        }
        if (photo.getCaption() != null && !photo.getCaption().isEmpty()){
            holder.caption.setText(photo.getCaption());
        } else {
            holder.caption.setVisibility(View.GONE);
        }
        final List<Comments> comments = photo.getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(mContext, CommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("Photo", photo);
                b.putExtra("commentcount",comments.size());
                b.putExtras(bundle);
                mContext.startActivity(b);

            }
        });

        String timestampDifference = getTimestampDifference(photo);
        if(!timestampDifference.equals("0")){
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        }else{
            holder.timeDetla.setText("TODAY");
        }

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        if (photo.getImage_Path() != null)
            imageLoader.displayImage(photo.getImage_Path(), holder.image);
        else
            holder.image.setVisibility(View.GONE);

        //get the profile image and username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Users")
                .orderByChild("userId")
                .equalTo(photo.getUser_id());
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

                        }
                    });
                    holder.avatar.setImageBitmap(getProfileImage(singleSnapshot.getValue(User.class).getImage()));
                    holder.avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent b = new Intent(mContext, CommentActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("Photo", photo);
                            b.putExtra("commentcount",comments.size());
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
        TextView username, timeDetla, caption, likes, comments,mTags;
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
                    .child("Post")
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
                            mReference.child("Post")
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
                    .child("Post")
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
    private String getTimestampDifference(Post photo){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_Created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
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

}
