package tech.android.jobsharing.activities;

import com.nostra13.universalimageloader.core.ImageLoader;

import tech.android.jobsharing.base.BaseActivity;
import tech.android.jobsharing.databinding.ActivityGroupsBinding;
import tech.android.jobsharing.models.Group;

public class GroupsActivity extends BaseActivity {
    private ActivityGroupsBinding binding;
    public static final String DATA_GROUP = "data_group";
    private Group group;

    @Override
    protected void initAction() {
        binding.imageBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    protected void initData() {
        group = getIntent().getParcelableExtra(DATA_GROUP);
        binding.textTitle.setText(group.getGroupName());
        binding.textGroupName.setText(group.getGroupName());
        binding.textGroupDateCreated.setText(group.getDateCreated());
        binding.textGroupDescription.setText(group.getGroupDescription());
        //load group background
        if (!group.getImagePath().isEmpty()){
            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(group.getImagePath(), binding.imageBackground);
        }
    }

    @Override
    protected void bindView() {
        binding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}