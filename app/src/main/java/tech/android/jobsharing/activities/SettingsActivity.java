package tech.android.jobsharing.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import tech.android.jobsharing.R;
import tech.android.jobsharing.databinding.SettingsActivityBinding;

public class SettingsActivity extends AppCompatActivity {
    private SettingsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final ListPreference prefListThemes = (ListPreference) findPreference("language_preference");
            assert prefListThemes != null;
            SharedPreferences sharedPref = getActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            String language = sharedPref.getString("language", "vi");
            prefListThemes.setValue(language);
            prefListThemes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("language", newValue.toString());
                    editor.commit();
                    startActivity(new Intent(getActivity(),SignInActivity.class));
                    getActivity().finishAffinity();
                    return true;
                }
            });
        }
    }
}