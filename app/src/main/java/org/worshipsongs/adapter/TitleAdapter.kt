package org.worshipsongs.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import java.util.*

/**
 * Author : Madasamy
 * Version : 3.x.x
 */

class TitleAdapter<T>(context: AppCompatActivity, @LayoutRes resource: Int) : ArrayAdapter<T>(context, resource)
{

    private var titleAdapterListener: TitleAdapterListener<T>? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        var view = convertView
        if (view == null)
        {
            val layoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.title_row, null)
        }
        setViews(view!!, position)
        return view
    }

    private fun setViews(view: View, position: Int)
    {
        val maps = HashMap<String, Any>()
        maps[CommonConstants.TITLE_KEY] = getTitlesView(view)
        maps[CommonConstants.SUBTITLE_KEY] = getSubtitleTextView(view)
        maps[CommonConstants.COUNT_KEY] = getCountView(view)
        maps[CommonConstants.PLAY_IMAGE_KEy] = getPlayImageView(view)
        maps[CommonConstants.OPTIONS_IMAGE_KEY] = getOptionsImageView(view)
        maps[CommonConstants.POSITION_KEY] = position
        maps[CommonConstants.SONG_BOOK_NAME_KEY] = getSongBookNameTextView(view)
        titleAdapterListener!!.setViews(maps, getItem(position))
    }

    private fun getTitlesView(view: View): TextView
    {
        return view.findViewById<View>(R.id.title_text_view) as TextView
    }

    private fun getCountView(view: View): TextView
    {
        return view.findViewById<View>(R.id.count_text_view) as TextView
    }

    private fun getSubtitleTextView(view: View): TextView
    {
        return view.findViewById<View>(R.id.subtitle_text_view) as TextView
    }

    private fun getPlayImageView(rowView: View): ImageView
    {
        return rowView.findViewById<View>(R.id.video_image_view) as ImageView
    }

    private fun getOptionsImageView(rowView: View): ImageView
    {
        return rowView.findViewById<View>(R.id.option_image_view) as ImageView
    }

    private fun getSongBookNameTextView(view: View): TextView
    {
        return view.findViewById<View>(R.id.songBookName_text_view) as TextView
    }

    fun addObjects(objects: List<T>)
    {
        clear()
        addAll(objects)
        notifyDataSetChanged()
    }

    fun setTitleAdapterListener(titleAdapterListener: TitleAdapterListener<T>)
    {
        this.titleAdapterListener = titleAdapterListener
    }

    interface TitleAdapterListener<T>
    {
      open  fun setViews(objects: Map<String, Any>, t: T?)
    }
}
