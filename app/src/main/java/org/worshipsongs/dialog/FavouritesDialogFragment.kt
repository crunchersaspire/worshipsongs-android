package org.worshipsongs.dialog



import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.domain.SongDragDrop
import org.worshipsongs.service.FavouriteService

/**
 * Author: Seenivasan, Madasamy
 * version :1.0.0
 */

class FavouritesDialogFragment : DialogFragment()
{
    private val favouriteService = FavouriteService()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val names = favouriteService.findNames()
        names.add(0, "New favourite...")
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.DialogTheme))
        builder.setTitle(getString(R.string.addToPlayList))
        builder.setItems(names.toTypedArray()) { dialog, which -> this@FavouritesDialogFragment.onClick(which, names) }
        return builder.create()
    }

    private fun onClick(which: Int, names: List<String>)
    {
        val args = arguments
        val songName = args!!.getString(CommonConstants.TITLE_KEY)
        val localisedName = args.getString(CommonConstants.LOCALISED_TITLE_KEY)
        val id = args.getInt(CommonConstants.ID)
        if (which == 0)
        {
            val addFavouritesDialogFragment = AddFavouritesDialogFragment.newInstance(args)
            addFavouritesDialogFragment.show(activity!!.supportFragmentManager, AddFavouritesDialogFragment::class.java.simpleName)
        } else
        {
            val songDragDrop = SongDragDrop(id.toLong(), songName!!, false)
            songDragDrop.tamilTitle = localisedName
            favouriteService.save(names[which], songDragDrop)
            Toast.makeText(activity, "Song added to favourite...!", Toast.LENGTH_LONG).show()
        }
    }

    companion object
    {

        fun newInstance(bundle: Bundle): FavouritesDialogFragment
        {
            val favouritesDialogFragment = FavouritesDialogFragment()
            favouritesDialogFragment.arguments = bundle
            return favouritesDialogFragment
        }
    }

}
