package org.worshipsongs.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.DragItem
import com.woxthebox.draglistview.DragListView
import org.apache.commons.lang3.StringUtils
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.activity.SongContentViewActivity
import org.worshipsongs.adapter.FavouriteSongAdapter
import org.worshipsongs.domain.Setting
import org.worshipsongs.domain.SongDragDrop
import org.worshipsongs.listener.SongContentViewListener
import org.worshipsongs.service.FavouriteService
import org.worshipsongs.service.PopupMenuService
import org.worshipsongs.service.SongService
import org.worshipsongs.service.UserPreferenceSettingService
import org.worshipsongs.utils.CommonUtils
import java.util.*

/**
 * Author : Madasamy
 * Version : 0.1.0
 */

class FavouriteSongsFragment : Fragment(), FavouriteSongAdapter.FavouriteListener
{
    private var configureDragDrops: MutableList<SongDragDrop>? = null
    private var favouriteSongAdapter: FavouriteSongAdapter? = null

    private var songContentViewListener: SongContentViewListener? = null
    private var favouriteService: FavouriteService? = null
    private var songService: SongService? = null
    private val popupMenuService = PopupMenuService()
    private val userPreferenceSettingService = UserPreferenceSettingService()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        favouriteService = FavouriteService()
        songService = SongService(context!!.applicationContext)
        val serviceName = arguments!!.getString(CommonConstants.SERVICE_NAME_KEY)
        val favourite = favouriteService!!.find(serviceName!!)
        configureDragDrops = favourite.dragDrops
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.favourite_song_layout, container, false)
        setDragListView(view)
        return view
    }

    private fun setDragListView(view: View)
    {
        val dragListView = view.findViewById<View>(R.id.drag_list_view) as DragListView
        dragListView.recyclerView.isVerticalScrollBarEnabled = true
        dragListView.setLayoutManager(LinearLayoutManager(context))
        favouriteSongAdapter = FavouriteSongAdapter(configureDragDrops!!)
        favouriteSongAdapter!!.setFavouriteListener(this)
        dragListView.setAdapter(favouriteSongAdapter!!, true)
        dragListView.setCanDragHorizontally(false)
        dragListView.setCustomDragItem(MyDragItem(context!!, R.layout.favourite_song_adapter))
        dragListView.setDragListListener(FavouriteDragListListener())
    }

    override fun onRemove(dragDrop: SongDragDrop)
    {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.DialogTheme))
        builder.setTitle(getString(R.string.remove_favourite_song_title))
        builder.setMessage(getString(R.string.remove_favourite_song_message))
        builder.setPositiveButton(R.string.yes) { dialog, which ->
            favouriteService!!.removeSong(arguments!!.getString(CommonConstants.SERVICE_NAME_KEY)!!, dragDrop.title!!)
            configureDragDrops!!.remove(dragDrop)
            favouriteSongAdapter!!.itemList = configureDragDrops
            favouriteSongAdapter!!.notifyDataSetChanged()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    override fun onClick(dragDrop: SongDragDrop)
    {
        val song = songService!!.findContentsByTitle(dragDrop.title!!)
        if (song != null)
        {
            val titles = ArrayList<String>()
            titles.add(dragDrop.title!!)
            if (CommonUtils.isPhone(context!!))
            {
                val intent = Intent(activity, SongContentViewActivity::class.java)
                val bundle = Bundle()
                bundle.putStringArrayList(CommonConstants.TITLE_LIST_KEY, titles)
                bundle.putInt(CommonConstants.POSITION_KEY, 0)
                Setting.instance.position = 0
                intent.putExtras(bundle)
                activity!!.startActivity(intent)
            } else
            {
                Setting.instance.position = favouriteSongAdapter!!.getPositionForItem(dragDrop)
                songContentViewListener!!.displayContent(dragDrop.title!!, titles, favouriteSongAdapter!!.getPositionForItem(dragDrop))
            }
        } else
        {
            val bundle = Bundle()
            bundle.putString(CommonConstants.TITLE_KEY, getString(R.string.warning))
            bundle.putString(CommonConstants.MESSAGE_KEY, getString(R.string.message_song_not_available, "\"" + getTitle(dragDrop) + "\""))
            val alertDialogFragment = AlertDialogFragment.newInstance(bundle)
            alertDialogFragment.setVisibleNegativeButton(false)
            alertDialogFragment.show(activity!!.supportFragmentManager, "WarningDialogFragment")
        }
    }

    private fun getTitle(dragDrop: SongDragDrop): String
    {
        return if (userPreferenceSettingService.isTamil && StringUtils.isNotBlank(dragDrop.tamilTitle)) dragDrop.tamilTitle!!
        else dragDrop.title!!
    }

    private class MyDragItem internal constructor(context: Context, layoutId: Int) : DragItem(context, layoutId)
    {

        override fun onBindDragView(clickedView: View, dragView: View)
        {
            val text = (clickedView.findViewById<View>(R.id.text) as TextView).text
            (dragView.findViewById<View>(R.id.text) as TextView).text = text
            //dragView.findViewById(R.id.item_layout).setBackgroundColor(dragView.getResources().getColor(R.color.list_item_background));
        }
    }

    private inner class FavouriteDragListListener : DragListView.DragListListener
    {
        override fun onItemDragStarted(position: Int)
        {
            //Do nothing
        }

        override fun onItemDragging(itemPosition: Int, x: Float, y: Float)
        {
            //Do nothing
        }

        override fun onItemDragEnded(fromPosition: Int, toPosition: Int)
        {
            favouriteService!!.save(arguments!!.getString(CommonConstants.SERVICE_NAME_KEY)!!, favouriteSongAdapter!!.itemList)
        }
    }

    fun setSongContentViewListener(songContentViewListener: SongContentViewListener)
    {
        this.songContentViewListener = songContentViewListener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        menu.clear()
        if (CommonUtils.isPhone(context!!))
        {
            inflater!!.inflate(R.menu.action_bar_options, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        Log.i(FavouriteSongsFragment::class.java.simpleName, "Menu item " + item.itemId + " " + R.id.options)
        when (item.itemId)
        {
            android.R.id.home ->
            {
                activity!!.finish()
                return true
            }
            R.id.options ->
            {
                popupMenuService.shareFavouritesInSocialMedia(activity as Activity, activity!!.findViewById(R.id.options),
                        arguments!!.getString(CommonConstants.SERVICE_NAME_KEY)!!)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object
    {

        fun newInstance(bundle: Bundle): FavouriteSongsFragment
        {
            val favouriteSongsFragment = FavouriteSongsFragment()
            favouriteSongsFragment.arguments = bundle
            return favouriteSongsFragment
        }
    }

}
