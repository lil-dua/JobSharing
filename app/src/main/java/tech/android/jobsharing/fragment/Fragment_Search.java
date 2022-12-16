package tech.android.jobsharing.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tech.android.jobsharing.R;
import tech.android.jobsharing.adapter.RecommendJobAdapter;
import tech.android.jobsharing.adapter.SearchJobAdapter;
import tech.android.jobsharing.models.Post;
import tech.android.jobsharing.models.User;

/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Search extends Fragment {

    private View view;
    private RecyclerView jobsRecycleView;
    private AppCompatImageView imgSearch;
    private EditText edtSearch;
    private ProgressBar progressBar;
    private List<Post> postList;
    private List<User> userList;
    private List<String> mFollowing;
    private int mResults;
    private List<Post> mPaginatedPhotos;

    private RecommendJobAdapter recommendJobAdapter;
    private SearchJobAdapter searchJobAdapter;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search,container,false);
        init();
//        getJobDetail();
        getListJob();
        setListeners();
        return view;
    }

    private void init(){
        jobsRecycleView = view.findViewById(R.id.jobsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        imgSearch = view.findViewById(R.id.imageSearch);
        edtSearch = view.findViewById(R.id.editTextSearch);

        jobsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        userList = new ArrayList<>();

    }

    private void setListeners() {
        imgSearch.setOnClickListener(v -> {
            processSearch(edtSearch.getText().toString());
        });
    }

    //search user list
    private void processSearch(String s){
        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Post").orderByChild("userId").startAt(s).endAt(s+"\uf8ff"), Post.class)
                        .build();
        searchJobAdapter = new SearchJobAdapter(getContext(),options);
        searchJobAdapter.startListening();
        jobsRecycleView.setAdapter(searchJobAdapter);
    }

    private void getJobDetail(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);

                }
                jobsRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
                recommendJobAdapter = new RecommendJobAdapter(getActivity(),userList);
                recommendJobAdapter.notifyDataSetChanged();
                jobsRecycleView.setAdapter(recommendJobAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast(error.getMessage());
            }
        });
    }

    private void getListJob(){
        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Post"), Post.class)
                        .build();
        searchJobAdapter = new SearchJobAdapter(getContext(),options);
        jobsRecycleView.setAdapter(searchJobAdapter);
    }

    //show Toast
    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
