package com.tangledbytes.screenfilter.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.tangledbytes.screenfilter.R;
import com.tangledbytes.screenfilter.utils.AppConstants;
import com.tangledbytes.screenfilter.utils.Utils;

import java.util.Objects;

public class AppPreferenceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_preference_activity);
        getSupportFragmentManager().beginTransaction().replace(R.id.parentView, new AppPreferenceFragment()).commit();
    }

    public static class AppPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            
            // Initialize prefs
            Preference prefSendFeedBack =
                    findPreference(AppConstants.PreferenceConstants.KEY_SEND_FEEDBACK);
            Preference prefAbout =
                    findPreference(AppConstants.PreferenceConstants.KEY_ABOUT);
            Preference prefResetSettings =
                    findPreference(AppConstants.PreferenceConstants.KEY_RESET_PREFS);

            // Watch for preference clicks
            if (prefSendFeedBack != null) {
                prefSendFeedBack.setOnPreferenceClickListener(preference -> {
                    Intent intentSendFeedback = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts("mailto", getString(R.string.developer_email_id), null));
                    intentSendFeedback.putExtra(Intent.EXTRA_SUBJECT, "Screen Filter Lite - Feedback");
                    StringBuilder mailBody = new StringBuilder();
                    mailBody.append("* App {\n");
                    mailBody.append(new Utils(getContext()).getAppInfo());
                    mailBody.append("\n}\n* Device {\n");
                    mailBody.append(Utils.getDeviceInfo());
                    mailBody.append("\n}");
                    intentSendFeedback.putExtra(Intent.EXTRA_TEXT, mailBody.toString());
                    startActivity(Intent.createChooser(intentSendFeedback, getString(R.string.chooser_title)));
                    return true;
                });
            }
            if (prefAbout != null) {
                prefAbout.setOnPreferenceClickListener(prefs -> {
                    startActivity(new Intent(getContext(), AboutActivity.class));
                    return true;
                });
            }
            if (prefResetSettings != null) {
                prefResetSettings.setOnPreferenceClickListener(prefs -> {
                    //TODO: Is it working changed from prefs.getEditor().clear().commit()
                    if (Objects.requireNonNull(prefs.getSharedPreferences()).edit().clear().commit()) {
                        Utils.showToast(getContext(), R.string.success_prefs_reset);
                        // TODO: Recreate activity
                        //recreate();
                    } else
                        Utils.showToast(getContext(), R.string.failure_prefs_reset);
                    return true;
                });
            }
        }
    }
}