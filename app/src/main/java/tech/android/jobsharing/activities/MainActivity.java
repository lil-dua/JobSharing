package tech.android.jobsharing.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import tech.android.jobsharing.R;
import tech.android.jobsharing.adapter.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private FloatingActionButton floatingActionButton;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setup();
        getToken();
        setListeners();
    }

    private void setListeners() {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
    private void init() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager2);
        floatingActionButton = findViewById(R.id.fabNewPost);

    }
    private void setup() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);
        //set up for Tab Layot
        new TabLayoutMediator(tabLayout,viewPager2,((tab, position) -> {
            switch (position){
                case 0:
                    // fragment home
                    tab.setIcon(R.drawable.ic_home_50);
                    break;
                case 1:
                    // fragment profile
                    tab.setIcon(R.drawable.ic_user);
                    break;
                case 2:
                    // fragment search
                    tab.setIcon(R.drawable.ic_group);
                    break;
                case 3:
                    // fragment notification
                    tab.setIcon(R.drawable.ic_job);
                    break;
//                case 4:
//                    // fragment notification
//                    tab.setIcon(R.drawable.ic_job);
//                    break;
            }
        })).attach();
        viewPager2.setOffscreenPageLimit(5);

    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(mUser.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child("fcmToken").setValue(token);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}