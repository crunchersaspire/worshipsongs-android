package org.worshipsongs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.worshipsongs.R;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.activity.UserSettingActivity;
import org.worshipsongs.preference.LanguagePreference;
import org.worshipsongs.preference.PreferenceListener;
import org.worshipsongs.preference.ThemeListPreference;

/**
 * Author:Seenivasan, Madasamy
 * version:1.0.0
 */

public class SettingsPreferenceFragment extends PreferenceFragment implements PreferenceListener
{
    private UserSettingActivity userSettingActivity = new UserSettingActivity();


    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        languagePreference("languagePreference");
        themePreference("themeKey");
        resetPreferenceSettings("resetDialog");
    }

    private void languagePreference(String key)
    {
        LanguagePreference resetDialogPreference = (LanguagePreference) findPreference(key);
        resetDialogPreference.setPreferenceListener(this);
    }

    private void themePreference(String key)
    {
        ThemeListPreference themeListPreference = (ThemeListPreference) findPreference(key);
        themeListPreference.setPreferenceListener(this);
    }

    public void resetPreferenceSettings(String preferenceKey)
    {
        Preference resetDialogPreference = findPreference(preferenceKey);
        resetDialogPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                Intent startIntent = new Intent(WorshipSongApplication.getContext(), UserSettingActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                userSettingActivity.activityFinish();
                startActivity(startIntent);
                return false;
            }
        });
    }

    public void setUserSettingActivity(UserSettingActivity userSettingActivity)
    {
        this.userSettingActivity = userSettingActivity;
    }

    @Override
    public void onSelect()
    {
        userSettingActivity.invalidateOptionsMenu();
        userSettingActivity.finish();
        Intent startIntent = new Intent(WorshipSongApplication.getContext(), UserSettingActivity.class);
        startActivity(startIntent);

    }
}