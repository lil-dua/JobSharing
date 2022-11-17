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
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.List;
import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.ChatActivity;
import tech.android.jobsharing.models.User;

/***
 * Created by HoangRyan aka LilDua on 11/16/2022.
 */
public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ViewHolder> {

    private final Context mContext;
    private List<User> userList;

    public RecentConversationAdapter(Context mContext,List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }


    @NonNull
    @Override
    public RecentConversationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_recent_conversion,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentConversationAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.getName());
        holder.imgProfile.setImageBitmap(getConversionImage(user.getImage()));
        //access to chat activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userId",user.getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public RoundedImageView imgProfile;
        public TextView txtName,txtLastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imageRecentProfile);
            txtName = itemView.findViewById(R.id.textRecentName);
            txtLastMessage = itemView.findViewById(R.id.textRecentMessage);
        }
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}
