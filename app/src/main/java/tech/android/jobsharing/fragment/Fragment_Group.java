package tech.android.jobsharing.fragment;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.CreateGroupActivity;
import tech.android.jobsharing.activities.GroupsActivity;
import tech.android.jobsharing.adapter.GroupAdapter;
import tech.android.jobsharing.models.Group;
import tech.android.jobsharing.utils.UniversalImageLoader;

/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Group extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private RecyclerView rcvGroups;
    private AppCompatImageView imgSearch;
    private FloatingActionButton fabNewGroup;
    private GroupAdapter groupAdapter;
    private ArrayList<String> mFollowing;
    private int mResults;
    private EditText edtSearch;
    private ProgressBar progressBar;
    private List<Group> groupList;
    private ArrayList<Group> mPaginatedGroups;
    int LAUNCH_SECOND_ACTIVITY = 1;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group,container,false);
        init();
        setListeners();
        initImageLoader();
        getFollowing();
        displayMoreGroup();
        groupList = new ArrayList<>();
        mPaginatedGroups = new ArrayList<>();
        mFollowing = new ArrayList<>();
        return view;
    }

    private void init(){
        rcvGroups = view.findViewById(R.id.rcvGroups);
        progressBar = view.findViewById(R.id.progressBar);
        imgSearch = view.findViewById(R.id.imageSearch);
        edtSearch = view.findViewById(R.id.editTextSearch);
        fabNewGroup = view.findViewById(R.id.fabNewGroup);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setListeners() {
        imgSearch.setOnClickListener(v -> {

        });
        fabNewGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
            startActivity(intent);
        });
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }
    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
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
    public void displayMoreGroup(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");
        try{
            if(groupList.size() > mResults && groupList.size() > 0){
                int iterations;
                if(groupList.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = groupList.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++){
                    mPaginatedGroups.add(groupList.get(i));
                }
                mResults = mResults + iterations;
                groupAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }

    private void getFollowing(){
        groupList = new ArrayList<>();
        mPaginatedGroups = new ArrayList<>();
        mFollowing = new ArrayList<>();
        Log.d(TAG, "getFollowing: searching for following");
        progressBar.setVisibility(View.VISIBLE);
        rcvGroups.setVisibility(View.GONE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child("Groups");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    DataSnapshot dataSnapshot1 = singleSnapshot.getChildren().iterator().next();
                    mFollowing.add(dataSnapshot1.child("userId").getValue().toString());
                }
                getGroupList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getGroupList(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++){
            final int count = i;
            Query query = reference
                    .child("Groups")
                    .child(mFollowing.get(i))
                    .orderByChild("userId")
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0) {
                        rcvGroups.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Group group = singleSnapshot.getValue(Group.class);
                        groupList.add(group);
                    }
                    if(count >= mFollowing.size() -1){
                        //display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void displayPhotos(){
        if(groupList != null){
            try{
                Collections.sort(groupList, (o1, o2) -> o2.getDateCreated().compareTo(o1.getDateCreated()));
                int iterations = groupList.size();

                if(iterations > 10){
                    iterations = 10;
                }
                mResults = 10;
                for(int i = 0; i < iterations; i++){
                    mPaginatedGroups.add(groupList.get(i));
                }
                groupAdapter = new GroupAdapter(getActivity(), mPaginatedGroups, group -> {
                    Intent intent = new Intent(getActivity(),GroupsActivity.class);
                    intent.putExtra(GroupsActivity.DATA_GROUP, group);
                    startActivity(intent);
                });
                rcvGroups.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rcvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
                rcvGroups.setAdapter(groupAdapter);
                groupAdapter.notifyDataSetChanged();

            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        getFollowing();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            swipeRefreshLayout.setRefreshing(false);
        },1000);
    }


}
