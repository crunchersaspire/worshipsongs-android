package org.worshipsongs.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.worshipsongs.CommonConstants;
import org.worshipsongs.R;
import org.worshipsongs.WorshipSongApplication;
import org.worshipsongs.fragment.HomeFragment;
import org.worshipsongs.service.PresentationScreenService;
import org.worshipsongs.service.SongService;
import org.worshipsongs.utils.CommonUtils;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

/**
 * author:Madasamy
 * version:2.1.0
 */
public class NavigationDrawerActivity extends MaterialNavigationDrawer
{
    private static final String SENDER_MAIL = "appfeedback@mcruncher.com";
    private static final int UPDATE_DB_REQUEST_CODE = 555;
    private PresentationScreenService presentationScreenService;
    private SharedPreferences sharedPreferences;

    @Override
    public void init(Bundle bundle)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getAll().containsKey(CommonConstants.NO_OF_SONGS)) {
            SongService songService = new SongService(WorshipSongApplication.getContext());
            sharedPreferences.edit().putLong(CommonConstants.NO_OF_SONGS, songService.count()).apply();
        }
        long noOfSongs = sharedPreferences.getLong(CommonConstants.NO_OF_SONGS, 0);
        presentationScreenService = new PresentationScreenService(this);
        this.addAccount(new MaterialAccount(this.getResources(), null, noOfSongs + " Songs are available", null, R.drawable.worshipsongs));
        HomeFragment homeFragment = HomeFragment.newInstance();
        homeFragment.setArguments(getIntent().getExtras());
        this.addSection(newSection(getString(R.string.home), R.drawable.ic_library_books_white, homeFragment));
        this.addSection(newSection(getString(R.string.update_songs), android.R.drawable.stat_sys_download, getUpdateDbIntent()));
        this.addSection(newSection(getString(R.string.settings), R.drawable.ic_settings_white, getSettings()));
        this.addSection(newSection(getString(R.string.rate_this_app), android.R.drawable.star_off, getRateThisAppOnClickListener()));
        this.addSection(newSection(getString(R.string.share), android.R.drawable.ic_menu_share, getShare()));
        this.addSection(newSection(getString(R.string.feedback), android.R.drawable.sym_action_email, getEmail()));
        this.addBottomSection(newSection(getString(R.string.version) + " " + CommonUtils.getProjectVersion(), getVersionOnClickListener()));
    }

    private MaterialSectionListener getUpdateDbIntent()
    {
        return new MaterialSectionListener()
        {
            @Override
            public void onClick(MaterialSection materialSection)
            {
                Intent updateSongs = new Intent(NavigationDrawerActivity.this, UpdateSongsDatabaseActivity.class);
                startActivityForResult(updateSongs, UPDATE_DB_REQUEST_CODE);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case UPDATE_DB_REQUEST_CODE:
                long noOfSongs = sharedPreferences.getLong(CommonConstants.NO_OF_SONGS, 0);
                if (this.getAccountList().size() > 0) {
                    this.getAccountAtCurrentPosition(0).setSubTitle(getString(R.string.noOfSongsAvailable, noOfSongs));
                    this.notifyAccountDataChanged();
                }
                break;
            default:
                break;
        }
    }

    private Intent getSettings()
    {
        return new Intent(NavigationDrawerActivity.this, UserSettingActivity.class);
    }

    private MaterialSectionListener getRateThisAppOnClickListener()
    {
        return new MaterialSectionListener()
        {
            @Override
            public void onClick(MaterialSection section)
            {
                Uri uri = Uri.parse("market://details?id=" + NavigationDrawerActivity.this.getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(getFlags());
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + NavigationDrawerActivity.this.getApplicationContext().getPackageName())));
                }
            }
        };
    }

    int getFlags()
    {
        return (Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    }

    private Intent getShare()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_description) + getString(R.string.app_download_info));
        shareIntent.setType("text/plain");
        Intent intent = Intent.createChooser(shareIntent, getString(R.string.share) + " " + getString(R.string.app_name) + " in");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private Intent getEmail()
    {
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setData(Uri.parse("mailto:" + SENDER_MAIL));
        mailIntent.putExtra(Intent.EXTRA_EMAIL, "");
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, getEmailSubject(getApplicationContext()));
        return Intent.createChooser(mailIntent, "");
    }

    String getEmailSubject(Context context)
    {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return String.format(context.getString(R.string.feedback_subject), versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getString(R.string.feedback);
        }
    }

    private MaterialSectionListener getVersionOnClickListener()
    {
        return new MaterialSectionListener()
        {
            @Override
            public void onClick(MaterialSection section)
            {
                //Do nothing when click on version
            }
        };
    }

    @Override
    public void onResume()
    {
        super.onResume();
        presentationScreenService.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        presentationScreenService.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presentationScreenService.onResume();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }
}
