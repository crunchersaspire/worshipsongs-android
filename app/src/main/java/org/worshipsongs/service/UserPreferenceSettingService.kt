package org.worshipsongs.service

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

import org.worshipsongs.CommonConstants
import org.worshipsongs.WorshipSongApplication

/**
 * Author: Seenivasan
 * Version: 1.0.0
 */
class UserPreferenceSettingService
{

    private val context = WorshipSongApplication.context
    private var sharedPreferences: SharedPreferences? = null

    val portraitFontSize: Float
        get() = sharedPreferences!!.getInt(CommonConstants.PRIMARY_FONT_SIZE_KEY, 18).toFloat()

    val landScapeFontSize: Float
        get() = sharedPreferences!!.getInt(CommonConstants.PRESENTATION_FONT_SIZE_KEY, 25).toFloat()

    val primaryColor: Int
        get()
        {
            val all = sharedPreferences!!.all
            val color: Int
            if (all.containsKey("primaryColor"))
            {
                color = Integer.parseInt(all["primaryColor"]!!.toString())
            } else
            {
                color = -12303292
            }
            return color
        }

    val secondaryColor: Int
        get()
        {
            val all = sharedPreferences!!.all
            val color: Int
            if (all.containsKey("secondaryColor"))
            {
                color = Integer.parseInt(all["secondaryColor"]!!.toString())
            } else
            {
                color = -65536
            }
            return color
        }

    val presentationBackgroundColor: Int
        get() = sharedPreferences!!.getInt("presentationBackgroundColor", -0x1000000)

    val presentationPrimaryColor: Int
        get() = sharedPreferences!!.getInt("presentationPrimaryColor", -0x1)

    val presentationSecondaryColor: Int
        get() = sharedPreferences!!.getInt("presentationSecondaryColor", -0x100)

    val isKeepAwake: Boolean
        get() = sharedPreferences!!.getBoolean("prefKeepAwakeOn", false)

    val isPlayVideo: Boolean
        get() = sharedPreferences!!.getBoolean("prefVideoPlay", true)

    val isTamilLyrics: Boolean
        get() = sharedPreferences!!.getBoolean("displayTamilLyrics", true)

    val isRomanisedLyrics: Boolean
        get() = sharedPreferences!!.getBoolean("displayRomanisedLyrics", true)

    val isTamil: Boolean
        get() = sharedPreferences!!.getInt(CommonConstants.LANGUAGE_INDEX_KEY, 0) == 0

    val displaySongBook: Boolean
        get() = sharedPreferences!!.getBoolean("prefDisplaySongbook", false)

    constructor()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    constructor(context: Context)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
}
