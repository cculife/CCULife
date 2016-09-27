package org.zankio.cculife.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import org.zankio.ccudata.ecourse.source.local.DatabaseBaseSource;
import org.zankio.cculife.Debug;
import org.zankio.cculife.R;
import org.zankio.cculife.Updater;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.utils.PackageUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends PreferenceActivity implements UserManager.onLoginStateChangedListener {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private static UserManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = UserManager.getInstance(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(this));
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_account);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_account);

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.perf_header_offline);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_offline);
        bindPreferenceSummaryToValue(findPreference("offline_mode"));

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_custom);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_custom);
        //bindPreferenceSummaryToValue(findPreference("update_course_list"));

        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_about);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_about);
        findPreference("about_title").setSummary(PackageUtils.getVersionName(this));
        // findPreference("check_update").setOnPreferenceClickListener(onPreferenceClickListener);
        // bindPreferenceSummaryToValue(findPreference("update_interval"));

        if (Debug.debug) {
            fakeHeader = new PreferenceCategory(this);
            fakeHeader.setTitle(R.string.pref_header_debug);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_debug);

            bindPreferenceSummaryToValue(findPreference("debug_ecourse_year"));
            bindPreferenceSummaryToValue(findPreference("debug_ecourse_term"));
        }

        sessionManager.setOnLoginStateChangedListener(this);
        loadAccountsSetting();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        Set<String> fragmentSet = new HashSet<>(Arrays.asList(
                "org.zankio.cculife.ui.SettingsActivity$AccountPreferenceFragment",
                "org.zankio.cculife.ui.SettingsActivity$CustomPreferenceFragment",
                "org.zankio.cculife.ui.SettingsActivity$OfflinePreferenceFragment",
                "org.zankio.cculife.ui.SettingsActivity$AboutPreferenceFragment"
        ));
        return fragmentSet.contains(fragmentName);
    }

    private void loadAccountsSetting(){
        Preference loginout = findPreference("account_log_in_out");

        assert loginout != null;
        loginout.setOnPreferenceClickListener(onPreferenceClickListener);

        onLoginStateChanged(sessionManager.isLogined());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sessionManager != null)
            sessionManager.setOnLoginStateChangedListener(null);
    }

    @Override
    public void onLoginStateChanged(boolean isLogined) {
        Preference loginout = findPreference("account_log_in_out");
        Preference user = findPreference("account_user");
        assert user != null;
        assert loginout != null;
        if (isLogined) {
            String username = sessionManager.getUsername();
            user.setSummary(username);
            loginout.setTitle("登出");
        } else {
            user.setSummary("未登入");
            loginout.setTitle("登入");
        }

    }


    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private static final Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();
            String key = preference.getKey();

            if (key == null || key.equals("")) return false;
            else if ("account_log_in_out".equals(key)) {
                sessionManager.toggleLogin();
                DatabaseBaseSource.clearData(context);
                org.zankio.ccudata.kiki.source.local.DatabaseBaseSource.clearData(context);
            } else if ("check_update".equals(key)) {
                new Updater(context).checkUpdate(true);
            }

            return false;
        }
    };

    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragment implements UserManager.onLoginStateChangedListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_account);
            sessionManager.setOnLoginStateChangedListener(this);

            loadAccountsSetting();
        }

        private void loadAccountsSetting(){
            Preference loginout = findPreference("account_log_in_out");
            assert loginout != null;
            loginout.setOnPreferenceClickListener(onPreferenceClickListener);
            onLoginStateChanged(sessionManager.isLogined());
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            sessionManager.setOnLoginStateChangedListener(null);
        }

        @Override
        public void onLoginStateChanged(boolean isLogined) {
            Preference loginout = findPreference("account_log_in_out");
            Preference user = findPreference("account_user");

            assert user != null;
            assert loginout != null;

            if (isLogined) {
                String username = sessionManager.getUsername();
                user.setSummary(username);
                loginout.setTitle("登出");
            } else {
                user.setSummary("未登入");
                loginout.setTitle("登入");
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            findPreference("about_title").setSummary(PackageUtils.getVersionName(getActivity()));
            //findPreference("check_update").setOnPreferenceClickListener(onPreferenceClickListener);
            //bindPreferenceSummaryToValue(findPreference("update_interval"));

            if (Debug.debug) {
                addPreferencesFromResource(R.xml.pref_debug);

                bindPreferenceSummaryToValue(findPreference("debug_ecourse_year"));
                bindPreferenceSummaryToValue(findPreference("debug_ecourse_term"));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class OfflinePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_offline);
            bindPreferenceSummaryToValue(findPreference("offline_mode"));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CustomPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_custom);
        }
    }
}
