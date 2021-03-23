package org.worshipsongs.dialog


import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.domain.SongDragDrop
import org.worshipsongs.service.FavouriteService

/**
 * @author: Seenivasan, Madasamy
 * @since :1.0.0
 */
class AddFavouritesDialogFragment : DialogFragment()
{
    private val favouriteService = FavouriteService()

    private val negativeOnClickListener: DialogInterface.OnClickListener
        get() = DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val layoutInflater = LayoutInflater.from(activity)
        val promptsView = layoutInflater.inflate(R.layout.add_service_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.DialogTheme))
        alertDialogBuilder.setView(promptsView)
        val serviceName = promptsView.findViewById<EditText>(R.id.service_name)
        alertDialogBuilder.setTitle(R.string.favourite_title)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(R.string.ok, getPositiveOnClickListener(serviceName))
        alertDialogBuilder.setNegativeButton(R.string.cancel, negativeOnClickListener)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        alertDialog.window!!.setBackgroundDrawableResource(R.color.white)
        return alertDialog
    }


    private fun getPositiveOnClickListener(serviceName: EditText): DialogInterface.OnClickListener
    {

        return DialogInterface.OnClickListener { dialog, which ->
            val args = arguments
            val songName = args!!.getString(CommonConstants.TITLE_KEY)
            val localisedName = args.getString(CommonConstants.LOCALISED_TITLE_KEY)
            val id = args.getInt(CommonConstants.ID)
            if (serviceName.text.toString() == "")
            {
                Toast.makeText(activity, "Enter favourite name...!", Toast.LENGTH_LONG).show()
            } else
            {
                val favouriteName = serviceName.text.toString()
                val songDragDrop = SongDragDrop(id.toLong(), songName!!, false)
                songDragDrop.tamilTitle = localisedName
                favouriteService.save(favouriteName, songDragDrop)
                Toast.makeText(activity, "Song added to favourite......!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

    }

    companion object
    {

        fun newInstance(bundle: Bundle): AddFavouritesDialogFragment
        {
            val addFavouritesDialogFragment = AddFavouritesDialogFragment()
            addFavouritesDialogFragment.arguments = bundle
            return addFavouritesDialogFragment
        }
    }
}
