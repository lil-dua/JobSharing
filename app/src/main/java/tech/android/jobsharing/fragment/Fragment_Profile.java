package tech.android.jobsharing.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.makeramen.roundedimageview.RoundedImageView;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.EditProfileActivity;
import tech.android.jobsharing.activities.SettingsActivity;


/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class Fragment_Profile extends Fragment {
    View view;
    RoundedImageView imageProfile, imageEditProfile;
    TextView txtName, txtDescription, numPost, numFollower, numFollowing;
    AppCompatImageView imgSetting;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile,container,false);
        //Init
        init();
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
        numPost =  view.findViewById(R.id.numPost);
        numFollower = view.findViewById(R.id.numFollower);
        numFollowing = view.findViewById(R.id.numFollowing);
    }

    private void setListeners() {
        //Setting
        imgSetting.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        //Edit profile
        imageEditProfile.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

    }


}
