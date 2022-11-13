package tech.android.jobsharing.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import tech.android.jobsharing.fragment.Fragment_Home;
import tech.android.jobsharing.fragment.Fragment_Notification;
import tech.android.jobsharing.fragment.Fragment_Profile;
import tech.android.jobsharing.fragment.Fragment_Search;


/***
 * Created by HoangRyan aka LilDua on 11/6/2022.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new Fragment_Profile();
            case 2:
                return new Fragment_Search();
            case 3:
                return new Fragment_Notification();
            case 0:
            default:
                return new Fragment_Home();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
