package org.worshipsongs.activity

import android.os.Bundle

import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import com.getbase.floatingactionbutton.FloatingActionButton

import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.adapter.PresentSongCardViewAdapter
import org.worshipsongs.service.SongService
import org.worshipsongs.domain.Song
import org.worshipsongs.service.PresentationScreenService

/**
 * Author : Madasamy
 * Version : 3.x
 */

class PresentSongActivity : AppCompatActivity()
{
    private var songService: SongService? = null
    private var song: Song? = null
    private var nextButton: FloatingActionButton? = null
    private var currentPosition: Int = 0
    private var previousButton: FloatingActionButton? = null
    private var listView: ListView? = null
    private var presentSongCardViewAdapter: PresentSongCardViewAdapter? = null
    private var presentationScreenService: PresentationScreenService? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.present_song_layout)
        initSetUp()
        presentationScreenService = PresentationScreenService(this@PresentSongActivity)
        presentationScreenService!!.showNextVerse(song, 0)
        setListView(song!!)
        setNextButton(song!!)
        setPreviousButton(song!!)
    }

    private fun initSetUp()
    {
        songService = SongService(this)
        val bundle = intent.extras
        val title = bundle!!.getString(CommonConstants.TITLE_KEY)
        song = songService!!.findContentsByTitle(title!!)
        setActionBar()
    }

    private fun setActionBar()
    {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = song!!.title
    }

    private fun setListView(song: Song)
    {
        listView = findViewById<View>(R.id.content_list) as ListView
        presentSongCardViewAdapter = PresentSongCardViewAdapter(this@PresentSongActivity, song.contents!!)
        presentSongCardViewAdapter!!.setItemSelected(0)
        listView!!.adapter = presentSongCardViewAdapter
        listView!!.onItemClickListener = ListViewOnItemClickListener()
    }

    private fun setNextButton(song: Song)
    {
        nextButton = findViewById<View>(R.id.next_verse_floating_button) as FloatingActionButton
        nextButton!!.visibility = View.VISIBLE
        nextButton!!.setOnClickListener(NextButtonOnClickListener(song))
    }

    private fun setPreviousButton(song: Song)
    {
        previousButton = findViewById<View>(R.id.previous_verse_floating_button) as FloatingActionButton
        previousButton!!.setOnClickListener(PreviousButtonOnClickListener(song))
    }

    public override fun onResume()
    {
        super.onResume()
        presentationScreenService!!.onResume()
    }

    public override fun onPause()
    {
        super.onPause()
        presentationScreenService!!.onPause()

    }

    public override fun onStop()
    {
        super.onStop()
        presentationScreenService!!.onStop()
    }

    override fun onBackPressed()
    {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            android.R.id.home -> finish()
            else ->
            {
            }
        }
        return true
    }

    private inner class ListViewOnItemClickListener : AdapterView.OnItemClickListener
    {

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long)
        {
            currentPosition = position
            presentationScreenService!!.showNextVerse(song, position)
            presentSongCardViewAdapter!!.setItemSelected(currentPosition)
            presentSongCardViewAdapter!!.notifyDataSetChanged()
            if (position == 0)
            {
                previousButton!!.visibility = View.GONE
                nextButton!!.visibility = View.VISIBLE
            } else if (song!!.contents!!.size == position + 1)
            {
                nextButton!!.visibility = View.GONE
                previousButton!!.visibility = View.VISIBLE
            } else
            {
                nextButton!!.visibility = View.VISIBLE
                previousButton!!.visibility = View.VISIBLE
            }
        }
    }

    private inner class NextButtonOnClickListener internal constructor(private val song: Song) : View.OnClickListener
    {

        override fun onClick(v: View)
        {
            currentPosition = currentPosition + 1
            if (song.contents!!.size == currentPosition)
            {
                nextButton!!.visibility = View.GONE
            }
            if (song.contents!!.size > currentPosition)
            {
                presentationScreenService!!.showNextVerse(song, currentPosition)
                listView!!.smoothScrollToPositionFromTop(currentPosition, 2)
                previousButton!!.visibility = View.VISIBLE
                presentSongCardViewAdapter!!.setItemSelected(currentPosition)
                presentSongCardViewAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private inner class PreviousButtonOnClickListener internal constructor(private val song: Song) : View.OnClickListener
    {

        override fun onClick(v: View)
        {
            currentPosition = currentPosition - 1
            if (currentPosition == song.contents!!.size)
            {
                currentPosition = currentPosition - 1
            }
            if (currentPosition <= song.contents!!.size && currentPosition >= 0)
            {
                presentationScreenService!!.showNextVerse(song, currentPosition)
                listView!!.smoothScrollToPosition(currentPosition, 2)
                nextButton!!.visibility = View.VISIBLE
                presentSongCardViewAdapter!!.setItemSelected(currentPosition)
                presentSongCardViewAdapter!!.notifyDataSetChanged()
            }
            if (currentPosition == 0)
            {
                previousButton!!.visibility = View.GONE
            }
        }
    }


}
