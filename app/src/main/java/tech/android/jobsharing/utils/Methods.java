package tech.android.jobsharing.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class Methods {

    //******************************METHOD TO GET POST COUNT***********************
    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot ds : dataSnapshot.child("User_Photo")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
    }
}
