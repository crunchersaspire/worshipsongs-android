package org.worshipsongs.fragment


import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.WorshipSongApplication
import org.worshipsongs.activity.SongContentViewActivity
import org.worshipsongs.adapter.TitleAdapter
import org.worshipsongs.domain.Setting
import org.worshipsongs.domain.Song
import org.worshipsongs.domain.Type
import org.worshipsongs.listener.SongContentViewListener
import org.worshipsongs.registry.ITabFragment
import org.worshipsongs.service.*
import org.worshipsongs.utils.CommonUtils
import org.worshipsongs.utils.ImageUtils
import java.util.*

/**
 * @author : Madasamy
 * @version : 3.x
 */

class SongsFragment : Fragment(), TitleAdapter.TitleAdapterListener<Song>, ITabFragment
{
    private var state: Parcelable? = null
    private var searchView: SearchView? = null
    private var filterMenuItem: MenuItem? = null
    private var songListView: ListView? = null
    private var songs: List<Song>? = null
    private var titleAdapter: TitleAdapter<Song>? = null
    private var songContentViewListener: SongContentViewListener? = null
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WorshipSongApplication.context)
    private val preferenceSettingService = UserPreferenceSettingService()
    private val popupMenuService = PopupMenuService()
    private var songService: SongService? = null
    private var songBookService: SongBookService? = null
    private var databaseService: DatabaseService? = null
    private var presentationScreenService: PresentationScreenService? = null
    private val userPreferenceSettingService = UserPreferenceSettingService()

    private val type: String
        get()
        {
            val bundle = arguments
            return if (bundle != null && bundle.containsKey(CommonConstants.TYPE))
            {
                bundle.getString(CommonConstants.TYPE, Type.SONG.name)
            } else
            {
                Type.SONG.name
            }
        }

    private val objectId: Int
        get()
        {
            val bundle = arguments
            return bundle?.getInt(CommonConstants.ID) ?: 0
        }

    private val searchViewCloseListener: SearchView.OnCloseListener
        get() = SearchView.OnCloseListener {
            filterMenuItem!!.isVisible = false
            false
        }

    private val searchViewClickListener: View.OnClickListener
        get() = View.OnClickListener { filterMenuItem!!.isVisible = true }

    private val queryTextListener: SearchView.OnQueryTextListener
        get() = object : SearchView.OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String): Boolean
            {
                updateObjects(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean
            {
                updateObjects(query)
                return true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null)
        {
            state = savedInstanceState.getParcelable(STATE_KEY)
        }
        databaseService = DatabaseService(activity!!)
        songService = SongService(activity!!.applicationContext)
        songBookService = SongBookService(activity!!.applicationContext)
        presentationScreenService = PresentationScreenService(activity!!)
        setHasOptionsMenu(true)
        initSetUp()
    }

    private fun initSetUp()
    {
        databaseService!!.open()
        loadSongs()
        if (!sharedPreferences.contains(CommonConstants.SEARCH_BY_TITLE_KEY))
        {
            sharedPreferences.edit().putBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true).apply()
        }
    }

    private fun loadSongs()
    {
        val type = type
        val id = objectId
        if (Type.AUTHOR.name.equals(type, ignoreCase = true))
        {
            songs = songService!!.findByAuthorId(id)
        } else if (Type.TOPICS.name.equals(type, ignoreCase = true))
        {
            songs = songService!!.findByTopicId(id)
        } else if (Type.SONG_BOOK.name.equals(type, ignoreCase = true))
        {
            songs = songService!!.findBySongBookId(id)
        } else
        {
            songs = songService!!.findAll()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.songs_layout, container, false)
        setListView(view)
        return view
    }


    private fun setListView(view: View)
    {
        songListView = view.findViewById<View>(R.id.song_list_view) as ListView
        titleAdapter = TitleAdapter((activity as AppCompatActivity?)!!, R.layout.songs_layout)
        titleAdapter!!.setTitleAdapterListener(this)
        updateObjects("")
        songListView!!.adapter = titleAdapter
        songListView!!.onItemClickListener = onItemClickListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.action_bar_menu, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu!!.findItem(R.id.menu_search).actionView as SearchView
        searchView!!.maxWidth = Integer.MAX_VALUE
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))

        val image = searchView!!.findViewById<View>(R.id.search_close_btn) as ImageView
        val drawable = image.drawable
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        searchView!!.setOnCloseListener(searchViewCloseListener)
        searchView!!.setOnSearchClickListener(searchViewClickListener)
        searchView!!.setOnQueryTextListener(queryTextListener)

        val searchByText = sharedPreferences.getBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true)
        searchView!!.queryHint = if (searchByText) getSearchByTitleOrNumberPlaceholder(type) else getString(R.string.hint_content)
        filterMenuItem = menu.getItem(0).setVisible(false)
        filterMenuItem!!.icon = ImageUtils.resizeBitmapImageFn(resources, BitmapFactory.decodeResource(resources, getResourceId(searchByText)), 35)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home ->
            {
                activity!!.finish()
                return true
            }
            R.id.filter ->
            {
                val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.DialogTheme))
                builder.setTitle(getString(R.string.search_title))
                builder.setCancelable(true)
                val title = if (Type.SONG_BOOK.name.equals(type, ignoreCase = true)) getString(R.string.search_title_or_content) else getString(R.string.search_type_title)
                builder.setItems(arrayOf(title, getString(R.string.search_type_content))) { dialog, which ->
                    if (which == 0)
                    {
                        searchView!!.queryHint = getSearchByTitleOrNumberPlaceholder(type)
                        sharedPreferences.edit().putBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true).apply()
                        item.icon = ImageUtils.resizeBitmapImageFn(resources, BitmapFactory.decodeResource(resources, getResourceId(true)), 35)
                    } else
                    {
                        searchView!!.queryHint = getString(R.string.hint_content)
                        sharedPreferences.edit().putBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, false).apply()
                        item.icon = ImageUtils.resizeBitmapImageFn(resources, BitmapFactory.decodeResource(resources, getResourceId(false)), 35)
                    }
                    searchView!!.setQuery(searchView!!.query, true)
                }
                builder.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getSearchByTitleOrNumberPlaceholder(type: String): String
    {
        return if (type.equals(Type.SONG_BOOK.name, ignoreCase = true))
        {
            getString(R.string.hint_title_or_number)
        } else
        {
            getString(R.string.hint_title)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu)
    {
        super.onPrepareOptionsMenu(menu)
    }

    override fun onResume()
    {
        super.onResume()
        if (sharedPreferences.getBoolean(CommonConstants.UPDATED_SONGS_KEY, false))
        {
            updateObjects("")
            sharedPreferences.edit().putBoolean(CommonConstants.UPDATED_SONGS_KEY, false).apply()
        } else if (state != null)
        {
            songListView!!.onRestoreInstanceState(state)
        } else
        {
            updateObjects("")
            titleAdapter!!.addObjects(songService!!.filterSongs(type, "", songs!!))
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean)
    {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser)
        {
            if (activity != null)
            {
                CommonUtils.hideKeyboard(activity)
            }
            if (searchView != null)
            {
                val searchByText = sharedPreferences.getBoolean(CommonConstants.SEARCH_BY_TITLE_KEY, true)
                searchView!!.queryHint = if (searchByText) getSearchByTitleOrNumberPlaceholder(type) else getString(R.string.hint_content)
            }
            if (filterMenuItem != null)
            {
                filterMenuItem!!.isVisible = false
            }

        }
    }

    internal fun getResourceId(searchByText: Boolean): Int
    {
        return if (searchByText) R.drawable.ic_titles else R.drawable.ic_content_paste
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        if (this.isAdded && songListView != null)
        {
            outState.putParcelable(STATE_KEY, songListView!!.onSaveInstanceState())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPause()
    {
        state = songListView!!.onSaveInstanceState()
        super.onPause()
    }

    override fun setViews(objects: Map<String, Any>, song: Song?)
    {
        val titleTextView = objects[CommonConstants.TITLE_KEY] as TextView?
        titleTextView!!.text = getTitle(song!!)
        val presentingSong = Setting.instance.song
        if (presentingSong != null && presentingSong.title == song.title && presentationScreenService!!.isPresentSong())
        {
            titleTextView.setTextColor(context!!.resources.getColor(R.color.light_navy_blue))
        } else
        {
            val typedValue = TypedValue()
            activity!!.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
            titleTextView.setTextColor(typedValue.data)
        }
        val subTitleTextView = objects[CommonConstants.SUBTITLE_KEY] as TextView?
        subTitleTextView!!.visibility = if (song.songBookNumber > 0) View.VISIBLE else View.GONE
        subTitleTextView.text = getString(R.string.song_book_no) + " " + song.songBookNumber

        val playImageView = objects[CommonConstants.PLAY_IMAGE_KEy] as ImageView?
        playImageView!!.visibility = if (isShowPlayIcon(song)) View.VISIBLE else View.GONE
        playImageView.setOnClickListener(imageOnClickListener(song.title!!))

        val optionsImageView = objects[CommonConstants.OPTIONS_IMAGE_KEY] as ImageView?
        optionsImageView!!.visibility = View.VISIBLE
        optionsImageView.setOnClickListener(imageOnClickListener(song.title!!))

        showSongBook(song, objects)
    }

    private fun showSongBook(song: Song, objects: Map<String, Any>)
    {
        val songBookNameList = songBookService?.findSongBookName(song.id)
        val songBookTextView = objects[CommonConstants.SONG_BOOK_NAME_KEY] as TextView?
        songBookTextView!!.visibility = if (canDisplaySongBook(songBookNameList)) View.VISIBLE else View.GONE
        songBookTextView.text = songBookNameList!!.joinToString()
    }

    private fun canDisplaySongBook(songBookNameList: List<String>?) =
            userPreferenceSettingService.displaySongBook && songBookNameList!!.isNotEmpty()

    private fun onItemClickListener(): AdapterView.OnItemClickListener
    {
        return AdapterView.OnItemClickListener { parent, view, position, id ->
            val song = titleAdapter!!.getItem(position)
            Setting.instance.position = 0
            val titleList = ArrayList<String>()
            titleList.add(song!!.title!!)
            val bundle = Bundle()
            bundle.putStringArrayList(CommonConstants.TITLE_LIST_KEY, titleList)
            if (songContentViewListener == null)
            {
                val intent = Intent(context, SongContentViewActivity::class.java)
                intent.putExtra(CommonConstants.SONG_BOOK_NUMBER_KEY, song.songBookNumber)
                intent.putExtras(bundle)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context!!.startActivity(intent)
            } else
            {
                songContentViewListener!!.displayContent(song.title!!, titleList, 0)
            }
        }
    }

    private fun getTitle(song: Song): String
    {
        try
        {
            return if (preferenceSettingService.isTamil && song.tamilTitle!!.length > 0) song.tamilTitle!!
            else song.title!!
        } catch (e: Exception)
        {
            return song.title!!
        }

    }

    internal fun isShowPlayIcon(song: Song): Boolean
    {
        val urlKey = song.urlKey
        return urlKey != null && urlKey.length > 0 && preferenceSettingService.isPlayVideo
    }

    private fun imageOnClickListener(title: String): View.OnClickListener
    {
        return View.OnClickListener { view -> popupMenuService.showPopupmenu(activity as AppCompatActivity, view, title, true) }
    }

    override fun defaultSortOrder(): Int
    {
        return 0
    }

    override val title: String
        get()
        {
            return "titles"
        }


    override fun checked(): Boolean
    {
        return true
    }

    override fun setListenerAndBundle(songContentViewListener: SongContentViewListener?, bundle: Bundle)
    {
        this.songContentViewListener = songContentViewListener
    }

    private fun updateObjects(query: String)
    {
        activity!!.runOnUiThread {
            if (titleAdapter != null)
            {
                titleAdapter!!.addObjects(songService!!.filterSongs(type, query, songs!!))
            }
        }
    }

    companion object
    {
        private val CLASS_NAME = SongsFragment::class.java.simpleName
        private val STATE_KEY = "listViewState"

        fun newInstance(bundle: Bundle): SongsFragment
        {
            val songsFragment = SongsFragment()
            songsFragment.arguments = bundle
            return songsFragment
        }
    }
}
