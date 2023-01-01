package tech.android.jobsharing.dialog;

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
import tech.android.jobsharing.databinding.FragmentExperienceDialogBinding;

/***
 * Created by HoangRyan aka LilDua on 1/1/2023.
 */
public class ExperienceDialog extends DialogFragment {

    private String type;
    private OnActionListener actionListener;
    private FragmentExperienceDialogBinding binding;

    public static ExperienceDialog newInstance(String type, OnActionListener actionListener){
        ExperienceDialog fragment = new ExperienceDialog();
        fragment.type =type;
        fragment.actionListener = actionListener;
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(Objects.equals(type,getString(R.string.none_experience))){
            binding.experienceDlgCbNone.setChecked(true);
            binding.experienceDlgCbUnderOne.setChecked(false);
            binding.experienceDlgCbThanOne.setChecked(false);
            binding.experienceDlgCbThanTwo.setChecked(false);
        }else if(Objects.equals(type,getString(R.string.under_1_year_experience))){
            binding.experienceDlgCbNone.setChecked(false);
            binding.experienceDlgCbUnderOne.setChecked(true);
            binding.experienceDlgCbThanOne.setChecked(false);
            binding.experienceDlgCbThanTwo.setChecked(false);
        }else if(Objects.equals(type,getString(R.string.than_1_year_experience))){
            binding.experienceDlgCbNone.setChecked(false);
            binding.experienceDlgCbUnderOne.setChecked(false);
            binding.experienceDlgCbThanOne.setChecked(true);
            binding.experienceDlgCbThanTwo.setChecked(false);
        }else{
            binding.experienceDlgCbNone.setChecked(false);
            binding.experienceDlgCbUnderOne.setChecked(false);
            binding.experienceDlgCbThanOne.setChecked(false);
            binding.experienceDlgCbThanTwo.setChecked(true);
        }

        binding.experienceDlgCbNone.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.none_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgLlNone.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.none_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgCbUnderOne.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.under_1_year_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgLlUnderOne.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.under_1_year_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgCbThanOne.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.than_1_year_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgLlThanOne.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.than_1_year_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgCbThanTwo.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.than_2_year_experience));
            dismissAllowingStateLoss();
        });
        binding.experienceDlgLlThanTwo.setOnClickListener(v -> {
            actionListener.onClick(getString(R.string.than_2_year_experience));
            dismissAllowingStateLoss();
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
            window.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.color.transparent));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExperienceDialogBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    public interface OnActionListener{
        void onClick(String type);
    }
}
