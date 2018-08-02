package com.ecommerce.merchant.fypproject;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

/**
 * Created by Eric on 22-Nov-17.
 */

public class SettingFragment extends PreferenceFragment {
    private String versionName = "";
    private FirebaseAuth firebaseAuth;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting_screen);

            firebaseAuth=FirebaseAuth.getInstance();//get firebase object
            if(firebaseAuth.getCurrentUser()==null){
                Intent intent = new Intent(getActivity().getApplicationContext(), SplashActivity.class);
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(getActivity(),getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
            }
        Preference version = findPreference("version_key");
        Preference Feedback = findPreference("feedback_key");
        Preference Logout = findPreference("logout_key");
        Preference About = findPreference("about_key");
        Preference ChangePassword = findPreference("change_password_key");

        Preference PrivacyPolicy = findPreference("privacypolicy_key");
        Preference contactUs=findPreference("contact_key");
        Preference TOS=findPreference("tos_key");

        try {
            PackageInfo packageInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version.setSummary(versionName);
        ChangePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });

        version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), versionName, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), FeedbackActivity.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });
        Logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                    String topic= firebaseAuth.getCurrentUser().getUid();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                    FirebaseAuth.getInstance().signOut();
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }

                    // user is now signed out
                Intent myIntent = new Intent(getActivity(), SplashActivity.class);
                myIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });

        About.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), AboutUs.class);
                startActivityForResult(myIntent, 0);
                return true;
            }
        });

        PrivacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), PrivacyPolicy.class);
                startActivity(myIntent);

                return true;
            }
        });
        contactUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), ContactUsActivity.class);
                startActivity(myIntent);
                return true;
            }
        });
        TOS.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(getActivity(), TermOfServicesActivity.class);
                startActivity(myIntent);
                return true;
            }
        });
    }
}
