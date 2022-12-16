package tech.android.jobsharing.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.ChatActivity;
import tech.android.jobsharing.models.ChatMessage;
import tech.android.jobsharing.models.User;

/***
 * Created by HoangRyan aka LilDua on 12/8/2022.
 */
public class SearchUserAdapter extends FirebaseRecyclerAdapter<User,SearchUserAdapter.UserViewHolder> {

    Context mContext;
    String theRecentMessage;
    public SearchUserAdapter(@NonNull Context mContext,@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
        this.mContext = mContext;
    }


    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull User user) {
        userViewHolder.txtName.setText(user.getName());
        userViewHolder.imgProfile.setImageBitmap(getConversionImage(user.getImage()));
        recentMessage(user.getUserId(),userViewHolder.txtRecentMessage);

        userViewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userId", user.getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_recent_conversion,parent,false);
        return new UserViewHolder(view);
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        public RoundedImageView imgProfile;
        public TextView txtName,txtRecentMessage;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imageRecentProfile);
            txtName = itemView.findViewById(R.id.textRecentName);
            txtRecentMessage = itemView.findViewById(R.id.textRecentMessage);
        }
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

    //check for recent message
    private void recentMessage(final String userId, final TextView recentMessage){
        theRecentMessage = "";
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(mUser != null && chatMessage != null){
                        if(chatMessage.getReceiver().equals(mUser.getUid()) && chatMessage.getSender().equals(userId)
                                || chatMessage.getReceiver().equals(userId) && chatMessage.getSender().equals(mUser.getUid())){
                            theRecentMessage = chatMessage.getMessage();
                        }
                    }
                }
                recentMessage.setText(theRecentMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
