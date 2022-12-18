package tech.android.jobsharing.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import tech.android.jobsharing.R;
import tech.android.jobsharing.databinding.ActivityPostJobBinding;
import tech.android.jobsharing.databinding.FragmentWorkplaceDialogBinding;

public class WorkplaceDialog extends DialogFragment {
    private String type ;
    private OnActionListener actionListener ;
    private FragmentWorkplaceDialogBinding binding;
    public static WorkplaceDialog newInstance(String type, OnActionListener actionListener) {
        WorkplaceDialog fragment = new WorkplaceDialog();
        fragment.type = type;
        fragment.actionListener = actionListener;
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Objects.equals(type, getString(R.string.on_site))){
            binding.workplaceDlgCbOnsite.setChecked(true);
            binding.workplaceDlgCbHybrid.setChecked(false);
            binding.workplaceDlgCbRemote.setChecked(false);
        } else if (Objects.equals(type, getString(R.string.hybrid))){
            binding.workplaceDlgCbOnsite.setChecked(false);
            binding.workplaceDlgCbHybrid.setChecked(true);
            binding.workplaceDlgCbRemote.setChecked(false);
        }else {
            binding.workplaceDlgCbOnsite.setChecked(false);
            binding.workplaceDlgCbHybrid.setChecked(false);
            binding.workplaceDlgCbRemote.setChecked(true);
        }
        binding.workplaceDlgCbOnsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.on_site));
                dismissAllowingStateLoss();
            }
        });
        binding.workplaceDlgLlOnSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.on_site));
                dismissAllowingStateLoss();
            }
        });
        binding.workplaceDlgCbHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.hybrid));
                dismissAllowingStateLoss();
            }
        });
        binding.workplaceDlgLlHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.hybrid));
                dismissAllowingStateLoss();
            }
        });
        binding.workplaceDlgCbRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.remote));
                dismissAllowingStateLoss();
            }
        });
        binding.workplaceDlgLlRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionListener.onCLick(getString(R.string.remote));
                dismissAllowingStateLoss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null){
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.setGravity(Gravity.BOTTOM);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.color.transparent));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWorkplaceDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    public interface OnActionListener{
        void onCLick(String type);
    }


}