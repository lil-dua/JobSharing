package tech.android.jobsharing.adapter;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import tech.android.jobsharing.R;
import tech.android.jobsharing.models.Group;


/***
 * Created by HoangRyan aka LilDua on 12/20/2022.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private Context mContext;
    private List<Group> groupList;
    private OnActionListener actionListener;
    DatabaseReference mReference;
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    public GroupAdapter(@NonNull Context mContext,@NonNull List<Group> groupList, OnActionListener actionListener) {
        this.mContext = mContext;
        this.groupList = groupList;
        this.actionListener = actionListener;
        mReference = FirebaseDatabase.getInstance().getReference();
    }
    public interface OnActionListener{
        void onClick(Group group);
    }
    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_group,parent,false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.txtName.setText(group.getGroupName());
        holder.txtDescription.setText(group.getGroupDescription());
        holder.txtDateCreated.setText("Date created: "+group.getDateCreated());
        //load image
        if (!group.getImagePath().isEmpty()){
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(group.getImagePath(), holder.imgGroup);
        }
        //access to group activity
        holder.itemView.setOnClickListener(v -> {
            actionListener.onClick(group);
        });
        if(reachedEndOfList(position)){
            loadMoreData();
        }
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder{
         RoundedImageView imgGroup;
         TextView txtName,txtDescription,txtDateCreated;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGroup = itemView.findViewById(R.id.imageGroup);
            txtName = itemView.findViewById(R.id.textGrName);
            txtDescription = itemView.findViewById(R.id.textGrDescription);
            txtDateCreated = itemView.findViewById(R.id.textDateCreated);
        }
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
}
