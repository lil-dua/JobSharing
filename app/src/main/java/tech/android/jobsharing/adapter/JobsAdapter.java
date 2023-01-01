package tech.android.jobsharing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
import tech.android.jobsharing.models.Job;
import tech.android.jobsharing.utils.TimeAgo;

public class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.ViewHolder>{
    public JobsAdapter(@NonNull Context context, @NonNull List<Job> objects, OnActionListener actionListener) {
        this.mContext = context;
        this.objects = objects;
        this.listener = actionListener;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    private String TAG = "NewFeedAdapter";
    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    public interface OnActionListener{
        void onClick(Job job);
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;
    private LayoutInflater mInflater;
    private Context mContext;
    private DatabaseReference mReference;
    private OnActionListener listener;
    private List<Job> objects;



    @NonNull
    @Override
    public JobsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobsAdapter.ViewHolder holder, int position) {
        Job job = objects.get(position);

        holder.time.setText(getTimestampDifference(job));
        holder.jobPosition.setText(job.getPosition());
        holder.company.setText(job.getCompany());
        holder.location.setText(job.getLocation());
        holder.jobType.setText("( "+job.getJobType() +")");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(job);
            }
        });
        if (!job.getImageCompany().isEmpty()){
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(job.getImageCompany(), holder.imvCompany);
        }
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
        TextView jobPosition,company, location, jobType, time;
        RoundedImageView imvCompany;
        View line;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobPosition = (TextView) itemView.findViewById(R.id.itJob_tvTitle);
            company = (TextView) itemView.findViewById(R.id.itJob_tvCompany);
            location = (TextView) itemView.findViewById(R.id.itJob_tvLocation);
            jobType = (TextView) itemView.findViewById(R.id.itJob_tvJobType);
            time = (TextView) itemView.findViewById(R.id.itJob_tvTime);
            line = (View) itemView.findViewById(R.id.itNewFeed_line);
            imvCompany = (RoundedImageView) itemView.findViewById(R.id.itJob_imvCompany);
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
    private String getTimestampDifference(Job job){
        TimeAgo timeAgo = new TimeAgo();
        final String photoTimestamp = job.getDateCreated();
        return timeAgo.covertTimeToText(photoTimestamp);
    }
}
