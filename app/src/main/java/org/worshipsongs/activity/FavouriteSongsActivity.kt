package org.worshipsongs.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.FrameLayout
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.fragment.FavouriteSongsFragment
import org.worshipsongs.fragment.HomeTabFragment
import org.worshipsongs.fragment.SongContentPortraitViewFragment
import org.worshipsongs.listener.SongContentViewListener
import org.worshipsongs.service.FavouriteService
import org.worshipsongs.service.PresentationScreenService
import org.worshipsongs.service.SongService
import java.util.*

/**
 * Author: Seenivasan, Madasamy
 * version 1.0.0
 */
class FavouriteSongsActivity : AbstractAppCompactActivity(), SongContentViewListener
{
    private var songContentFrameLayout: FrameLayout? = null
    private var presentationScreenService: PresentationScreenService? = null
    private var preferences: SharedPreferences? = null
    private var favouriteService: FavouriteService? = null
    private var songService: SongService? = null
    private val favouriteName: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        presentationScreenService = PresentationScreenService(this@FavouriteSongsActivity)
        preferences = PreferenceManager.getDefaultSharedPreferences(this@FavouriteSongsActivity)
        favouriteService = FavouriteService()
        songService = SongService(this@FavouriteSongsActivity)
        setActionBar()
        setContentViewFragment()
        setTabsFragment()
        displayHelpActivity()
    }

    private fun setActionBar()
    {
        setCustomActionBar()
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar.setDisplayShowTitleEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.title = getFavouriteName()
    }

    private fun setContentViewFragment()
    {
        songContentFrameLayout = findViewById<FrameLayout>(R.id.song_content_fragment)
    }

    private fun setTabsFragment()
    {
        val fragmentManager = supportFragmentManager
        val existingServiceSongsFragment = fragmentManager.findFragmentByTag(FavouriteSongsFragment::class.java.simpleName) as FavouriteSongsFragment?
        if (existingServiceSongsFragment == null)
        {
            val bundle = Bundle()
            bundle.putString(CommonConstants.SERVICE_NAME_KEY, getFavouriteName())
            val serviceSongsFragment = FavouriteSongsFragment.newInstance(bundle)
            serviceSongsFragment.setSongContentViewListener(this)
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.tabs_fragment, serviceSongsFragment, HomeTabFragment::class.java.simpleName)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun getFavouriteName(): String
    {
        return favouriteName ?: intent.getStringExtra(CommonConstants.SERVICE_NAME_KEY)!!
    }

    private fun displayHelpActivity()
    {
        if (!preferences!!.getBoolean(CommonConstants.DISPLAY_FAVOURITE_HELP_ACTIVITY, false))
        {
            startActivity(Intent(this, FavouriteSongsHelpActivity::class.java))
        }
    }

    override fun displayContent(title: String, titleList: List<String>, position: Int)
    {
        if (songContentFrameLayout != null)
        {
            val titles = ArrayList<String>()
            titles.add(title)
            val songContentPortraitViewFragment = SongContentPortraitViewFragment.newInstance(title, titles)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.song_content_fragment, songContentPortraitViewFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    public override fun onResume()
    {
        super.onResume()
        presentationScreenService!!.onResume()
    }

    override fun onStop()
    {
        super.onStop()
        presentationScreenService!!.onStop()
    }

    override fun onPause()
    {
        super.onPause()
        presentationScreenService!!.onPause()
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }
}
