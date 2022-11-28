package tech.android.jobsharing.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.EditProfileActivity;
import tech.android.jobsharing.activities.SettingsActivity;
import tech.android.jobsharing.activities.WebViewActivity;
import tech.android.jobsharing.models.User;


/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Profile extends Fragment {
    View view;
    RoundedImageView imageProfile, imageEditProfile;
    TextView txtName, txtDescription,txtLink, numPost, numFollower, numFollowing;
    AppCompatImageView imgSetting;
    FrameLayout frameNoPost;
    ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile,container,false);
        //Init
        init();
        getUserDetails();
        //set event listeners
        setListeners();
        return view;
    }
    private void init() {
        imgSetting = view.findViewById(R.id.imageSetting);
        imageProfile = view.findViewById(R.id.imageProfile);
        imageEditProfile = view.findViewById(R.id.imageEditProfile);
        txtName = view.findViewById(R.id.txtUserName);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtLink = view.findViewById(R.id.txtLink);
        numPost =  view.findViewById(R.id.numPost);
        numFollower = view.findViewById(R.id.numFollower);
        numFollowing = view.findViewById(R.id.numFollowing);
        frameNoPost = view.findViewById(R.id.frameNoPost);
        progressBar = view.findViewById(R.id.progressBar);
        frameNoPost.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);


    }

    private void setListeners() {
        //Setting
        imgSetting.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        //Edit profile
        imageEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

    }

    private void getUserDetails(){

        if(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.keepSynced(true);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final User user = snapshot.getValue(User.class);
                    txtDescription.setText(user.getDescription());
                    txtLink.setText(user.getLink());
                    //check null name
                    if(user.getName() != null){
                        txtName.setText(user.getName());
                    }
                    //check null image
                    if (user.getImage() != null) {
                        imageProfile.setImageBitmap(getProfileImage(user.getImage()));
                    }

                    //text link
                    txtLink.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra("link", "https://" + user.getLink());
                        startActivity(intent);
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            showToast("User id was not found!");
        }
    }

    private Bitmap getProfileImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

    //show Toast
    private void showToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }



}
