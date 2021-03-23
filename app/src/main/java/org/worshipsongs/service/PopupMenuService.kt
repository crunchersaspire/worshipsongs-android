package org.worshipsongs.service

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context.PRINT_SERVICE
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import androidx.core.content.FileProvider

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import org.apache.commons.lang3.StringUtils
import org.worshipsongs.BuildConfig
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.WorshipSongApplication
import org.worshipsongs.WorshipSongApplication.Companion.context
import org.worshipsongs.activity.CustomYoutubeBoxActivity
import org.worshipsongs.activity.PresentSongActivity
import org.worshipsongs.dialog.FavouritesDialogFragment
import org.worshipsongs.domain.Song
import org.worshipsongs.utils.PermissionUtils
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * author: Madasamy,Seenivasan, Vignesh Palanisamy
 * version: 1.0.0
 */

class PopupMenuService
{

    private val customTagColorService = CustomTagColorService()
    private val preferenceSettingService = UserPreferenceSettingService()
    private val favouriteService = FavouriteService()
    private val songService = SongService(context!!)

    fun showPopupmenu(activity: AppCompatActivity, view: View, songName: String, hidePlay: Boolean)
    {
        val wrapper = ContextThemeWrapper(context, R.style.PopupMenu_Theme)
        val popupMenu: PopupMenu
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            popupMenu = PopupMenu(wrapper, view, Gravity.RIGHT)
        } else
        {
            popupMenu = PopupMenu(wrapper, view)
        }
        popupMenu.menuInflater.inflate(R.menu.favourite_share_option_menu, popupMenu.menu)
        val song = songService.findContentsByTitle(songName)
        val urlKey = song!!.urlKey
        val menuItem = popupMenu.menu.findItem(R.id.play_song)
        menuItem.isVisible = urlKey != null && urlKey.length > 0 && preferenceSettingService.isPlayVideo && hidePlay
        val exportMenuItem = popupMenu.menu.findItem(R.id.export_pdf)
        exportMenuItem.isVisible = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
        val presentSongMenuItem = popupMenu.menu.findItem(R.id.present_song)
        presentSongMenuItem.isVisible = false
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId)
            {
                R.id.addToList ->
                {
                    val bundle = Bundle()
                    bundle.putString(CommonConstants.TITLE_KEY, songName)
                    bundle.putString(CommonConstants.LOCALISED_TITLE_KEY, song.tamilTitle)
                    bundle.putInt(CommonConstants.ID, song.id)
                    val favouritesDialogFragment = FavouritesDialogFragment.newInstance(bundle)
                    favouritesDialogFragment.show(activity.supportFragmentManager, "FavouritesDialogFragment")
                    true
                }
                R.id.share_whatsapp ->
                {
                    shareSongInSocialMedia(songName, song)
                    true
                }
                R.id.play_song ->
                {
                    showYouTube(urlKey, songName)
                    true
                }
                R.id.present_song ->
                {
                    startPresentActivity(songName)
                    true
                }
                R.id.export_pdf ->
                {
                    if (PermissionUtils.isStoragePermissionGranted(activity))
                    {
                        exportSongToPDF(songName, Arrays.asList(song))
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun shareSongInSocialMedia(songName: String, song: Song)
    {
        val builder = StringBuilder()
        builder.append(songName).append("\n").append("\n")
        for (content in song.contents!!)
        {
            builder.append(customTagColorService.getFormattedLines(content))
            builder.append("\n")
        }
        builder.append(context!!.getString(R.string.share_info))
        Log.i(this@PopupMenuService.javaClass.simpleName, builder.toString())
        val textShareIntent = Intent(Intent.ACTION_SEND)
        textShareIntent.putExtra(Intent.EXTRA_TEXT, builder.toString())
        textShareIntent.type = "text/plain"
        val intent = Intent.createChooser(textShareIntent, "Share $songName with...")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(intent)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun exportSongToPDF(songName: String, songs: List<Song>)
    {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "$songName.pdf")

        val printAttrs = PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR).setMediaSize(PrintAttributes.MediaSize.ISO_A4).setResolution(PrintAttributes.Resolution("zooey", PRINT_SERVICE, 450, 700)).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
        val document = PrintedPdfDocument(WorshipSongApplication.context!!, printAttrs)
        for (i in songs.indices)
        {
            val song = songs[i]
            val pageInfo = PdfDocument.PageInfo.Builder(450, 700, i).create()
            var page: PdfDocument.Page? = document.startPage(pageInfo)
            if (page != null)
            {
                val titleDesign = Paint()
                titleDesign.textAlign = Paint.Align.LEFT
                titleDesign.textSize = 18f
                val title = getTamilTitle(song) + song.title!!
                val titleLength = titleDesign.measureText(title)
                var yPos = 50f
                if (page.canvas.width > titleLength)
                {
                    val xPos = page.canvas.width / 2 - titleLength.toInt() / 2
                    page.canvas.drawText(title, xPos.toFloat(), 20f, titleDesign)
                } else
                {
                    var xPos = page.canvas.width / 2 - titleDesign.measureText(song.tamilTitle).toInt() / 2
                    page.canvas.drawText(song.tamilTitle!! + "/", xPos.toFloat(), 20f, titleDesign)
                    xPos = page.canvas.width / 2 - titleDesign.measureText(song.title).toInt() / 2
                    page.canvas.drawText(song.title!!, xPos.toFloat(), 45f, titleDesign)
                    yPos = 75f
                }
                for (content in song.contents!!)
                {
                    if (yPos > 620)
                    {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        yPos = 40f
                    }
                    yPos = customTagColorService.getFormattedPage(content, page!!, 10f, yPos)
                    yPos = yPos + 20
                }
            }
            document.finishPage(page)
        }
        try
        {
            val os = FileOutputStream(file)
            document.writeTo(os)
            document.close()
            os.close()
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            val uriForFile = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID + ".provider", file)
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriForFile)
            val intent = Intent.createChooser(shareIntent, "Share $songName with...")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
            Log.i("done", file.absolutePath.toString())

        } catch (ex: Exception)
        {
            Log.e(PopupMenuService::class.java.simpleName, "Error occurred while exporting to PDF", ex)
        }

    }

    private fun getTamilTitle(song: Song): String
    {
        return if (StringUtils.isNotBlank(song.tamilTitle)) song.tamilTitle!! + "/" else ""
    }

    private fun showYouTube(urlKey: String?, songName: String)
    {
        Log.i(this.javaClass.simpleName, "Url key: " + urlKey!!)
        val youTubeIntent = Intent(context, CustomYoutubeBoxActivity::class.java)
        youTubeIntent.putExtra(CustomYoutubeBoxActivity.KEY_VIDEO_ID, urlKey)
        youTubeIntent.putExtra(CommonConstants.TITLE_KEY, songName)
        youTubeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(youTubeIntent)
    }

    private fun startPresentActivity(title: String)
    {
        val intent = Intent(context, PresentSongActivity::class.java)
        intent.putExtra(CommonConstants.TITLE_KEY, title)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(intent)
    }

    fun shareFavouritesInSocialMedia(activity: Activity, view: View, favouriteName: String)
    {
        val wrapper = ContextThemeWrapper(context, R.style.PopupMenu_Theme)
        val popupMenu: PopupMenu
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            popupMenu = PopupMenu(wrapper, view, Gravity.RIGHT)
        } else
        {
            popupMenu = PopupMenu(wrapper, view)
        }
        popupMenu.menuInflater.inflate(R.menu.favourite_share_option_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.play_song).isVisible = false
        popupMenu.menu.findItem(R.id.present_song).isVisible = false
        popupMenu.menu.findItem(R.id.addToList).isVisible = false
        popupMenu.setOnMenuItemClickListener(getPopupMenuItemListener(activity, favouriteName))
        popupMenu.show()
    }

    private fun getPopupMenuItemListener(activity: Activity, text: String): PopupMenu.OnMenuItemClickListener
    {
        return PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId)
            {
                R.id.share_whatsapp ->
                {
                    shareFavouritesInSocialMedia(text)
                    true
                }
                R.id.export_pdf ->
                {
                    if (PermissionUtils.isStoragePermissionGranted(activity))
                    {
                        exportSongToPDF(text, favouriteService.findSongsByFavouriteName(text))
                    }
                    false
                }

                else -> false
            }
        }
    }

    private fun shareFavouritesInSocialMedia(text: String)
    {
        val textShareIntent = Intent(Intent.ACTION_SEND)
        textShareIntent.putExtra(Intent.EXTRA_TEXT, favouriteService.buildShareFavouriteFormat(text))
        textShareIntent.type = "text/plain"
        val intent = Intent.createChooser(textShareIntent, "Share with...")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(intent)
    }
}
