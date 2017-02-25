package org.worshipsongs.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.worshipsongs.worship.BuildConfig;
import org.worshipsongs.worship.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author : Madasamy
 * Version : 3.x
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 22)
public class DatabaseFragmentTest
{
    private DatabaseFragment databaseFragment;


    @Before
    public void setUp()
    {
        databaseFragment = new DatabaseFragment();
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                .create()
                .start()
                .resume()
                .get();

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(databaseFragment, null);
        fragmentTransaction.commit();
    }

    @Test
    public void testImportDatabaseButton_properties()
    {
        System.out.println("--importDatabaseButton--");
        Button importDataBaseButton = (Button) databaseFragment.getView().findViewById(R.id.upload_database_button);
        assertEquals("Import database", importDataBaseButton.getText());
        assertEquals(-1, importDataBaseButton.getTextColors().getDefaultColor());
    }


    @Test
    public void testOnClickImportDatabaseButton()
    {
        System.out.println("--onClickImportDatabaseButton--");
        Button importDataBaseButton = (Button) databaseFragment.getView().findViewById(R.id.upload_database_button);
        assertTrue(importDataBaseButton.performClick());
        //AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        //ShadowAlertDialog shadowAlertDialog = shadowOf(latestAlertDialog);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void testRevertToDefaultDatabaseButton()
    {
        System.out.println("--revertToDefaultDatabaseButton--");
        Button defaultDatabaseButton = (Button) databaseFragment.getView().findViewById(R.id.default_database_button);
        assertEquals(30.0, defaultDatabaseButton.getElevation(), 0);
        assertEquals(0, defaultDatabaseButton.getVisibility());
    }

    @Test
    public void testOnClickRevertToDefaultDatabaseButton()
    {
        System.out.println("--revertToDefaultDatabaseButton--");
        Button defaultDatabaseButton = (Button) databaseFragment.getView().findViewById(R.id.default_database_button);
        assertTrue(defaultDatabaseButton.performClick());
    }

    @Test
    public void testProperties()
    {
        assertEquals("Are you sure want to revert to default database ?", databaseFragment.getString(R.string.message_database_confirmation));
    }


}