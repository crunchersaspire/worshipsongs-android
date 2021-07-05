package org.worshipsongs.service

import android.content.Context
import android.database.Cursor
import org.apache.commons.lang3.StringUtils
import org.worshipsongs.domain.SongBook
import java.util.*

/**
 * Author : Madasamy
 * Version : 3.x.x
 */

class SongBookService(context: Context)
{
    var allColumns = arrayOf("id", "name", "publisher")
    private val databaseService: DatabaseService
    private val userPreferenceSettingService: UserPreferenceSettingService

    init
    {
        databaseService = DatabaseService(context)
        userPreferenceSettingService = UserPreferenceSettingService(context)
    }

    fun findAll(): List<SongBook>
    {
        val songBooks = ArrayList<SongBook>()

        val query = "select songbook.id, songbook.name, songbook.publisher, count(songbook.id) " + "from songs as song inner join songs_songbooks as songsongbooks on " + "songsongbooks.song_id=song.id inner join song_books as songbook on " + "songbook.id=songsongbooks.songbook_id group by songbook.name"
        val cursor = databaseService.database!!.rawQuery(query, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast)
        {
            val songBook = cursorToSongBook(cursor)
            songBooks.add(songBook)
            cursor.moveToNext()
        }
        cursor.close()
        return songBooks
    }

    fun findSongBookName(songId: Int): List<String>
    {
        val songBookNames = ArrayList<String>()

        val query = "select songBook.name from song_books as songBook, songs_songbooks as songsSongBook " +
                "where songsSongBook.song_id = ? and songsSongBook.songbook_id = songBook.id"
        val cursor = databaseService.database!!.rawQuery(query, arrayOf(songId.toString()))
        cursor.moveToFirst()

        while (!cursor.isAfterLast)
        {
            val songBookName = cursor.getString(0)
            songBookNames.add(parseName(songBookName!!).trim())
            cursor.moveToNext()
        }
        cursor.close()
        return songBookNames
    }

    private fun cursorToSongBook(cursor: Cursor): SongBook
    {
        val songBook = SongBook()
        songBook.id = cursor.getInt(0)
        songBook.name = parseName(cursor.getString(1))
        songBook.publisher = cursor.getString(2)
        songBook.noOfSongs = cursor.getInt(3)
        return songBook
    }

    internal fun parseName(name: String): String
    {
        return if (userPreferenceSettingService.isTamil)
        {
            databaseService.parseTamilName(name)
        } else
        {
            databaseService.parseEnglishName(name)
        }
    }

    fun filteredSongBooks(query: String, songBooks: List<SongBook>): List<SongBook>
    {
        val filteredTextList = ArrayList<SongBook>()
        if (StringUtils.isBlank(query))
        {
            filteredTextList.addAll(songBooks)
        } else
        {
            for (songBook in songBooks)
            {
                if (songBook.name!!.toLowerCase().contains(query.toLowerCase()))
                {
                    filteredTextList.add(songBook)
                }
            }
        }
        return filteredTextList
    }

    companion object
    {
        val TABLE_NAME = "song_books"
    }
}
