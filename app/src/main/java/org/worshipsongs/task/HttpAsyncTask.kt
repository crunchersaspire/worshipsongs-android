package org.worshipsongs.task


import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.worshipsongs.CommonConstants
import org.worshipsongs.R
import org.worshipsongs.fragment.AlertDialogFragment
import org.worshipsongs.parser.CommitMessageParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Author : Madasamy
 * Version : 3.x
 */

class HttpAsyncTask(private val context: AppCompatActivity) : AsyncTask<String, String, String>(), AlertDialogFragment.DialogListener
{

    private val progressDialog: ProgressDialog?
    private var sharedPreferences: SharedPreferences? = null
    private val commitMessageParser = CommitMessageParser()

    private val remoteUrl: String
        get() = sharedPreferences!!.getString(CommonConstants.REMOTE_URL, context.getString(R.string.remoteUrl))!!

    init
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        progressDialog = ProgressDialog(context)
    }

    override fun onPreExecute()
    {
        super.onPreExecute()
        progressDialog!!.setMessage(context.getString(R.string.check_update_message))
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    override fun doInBackground(vararg params: String): String?
    {
        val stringUrl = params[0]
        val result: String
        var inputLine: String? = null
        try
        {
            val myUrl = URL(stringUrl)
            Log.i(CLASS_NAME, "Preparing to check download " + stringUrl + "")
            val connection = myUrl.openConnection() as HttpURLConnection
            connection.requestMethod = REQUEST_METHOD
            connection.readTimeout = READ_TIMEOUT
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.connect()
            val streamReader = InputStreamReader(connection.inputStream)
            val reader = BufferedReader(streamReader)
            val stringBuilder = StringBuilder()
            while ({ inputLine = reader.readLine(); inputLine }() != null)
            {
                stringBuilder.append(inputLine)
            }
            //Close our InputStream and Buffered reader
            reader.close()
            streamReader.close()
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString()
            return result
        } catch (e: Exception)
        {
            Log.e(CLASS_NAME, "Error", e)
            return ""
        }
    }

    override fun onPostExecute(jsonObject: String)
    {
        super.onPostExecute(jsonObject)
        val shaKey = commitMessageParser.getShaKey(jsonObject)
        val existingShaKey = sharedPreferences!!.getString(CommonConstants.COMMIT_SHA_KEY, "")
        if (!context.isFinishing && progressDialog != null && progressDialog.isShowing)
        {
            progressDialog.dismiss()
        }
        displayAlertDialog(shaKey, existingShaKey!!)
    }

    private fun displayAlertDialog(shaKey: String?, existingShaKey: String)
    {
        val bundle = Bundle()
        bundle.putString(CommonConstants.COMMIT_SHA_KEY, shaKey)
        bundle.putString(CommonConstants.TITLE_KEY, context.getString(R.string.updates_title))
        if (shaKey == null || shaKey.isEmpty())
        {
            bundle.putString(CommonConstants.TITLE_KEY, context.getString(R.string.warning))
            bundle.putString(CommonConstants.MESSAGE_KEY, "Error occurred while checking song updates")
            val alertDialogFragment = AlertDialogFragment.newInstance(bundle)
            alertDialogFragment.isCancelable = false
            alertDialogFragment.setVisibleNegativeButton(false)
            alertDialogFragment.setDialogListener(this)
            alertDialogFragment.show(context.supportFragmentManager, "MessageUpdateFragment")
        } else if (existingShaKey.equals(shaKey, ignoreCase = true))
        {
            bundle.putString(CommonConstants.MESSAGE_KEY, context.getString(R.string.message_no_update))
            val alertDialogFragment = AlertDialogFragment.newInstance(bundle)
            alertDialogFragment.isCancelable = false
            alertDialogFragment.setVisibleNegativeButton(false)
            alertDialogFragment.setDialogListener(this)
            alertDialogFragment.show(context.supportFragmentManager, "NoUpdateFragment")
        } else
        {
            bundle.putString(CommonConstants.MESSAGE_KEY, context.getString(R.string.message_update_available))
            val alertDialogFragment = AlertDialogFragment.newInstance(bundle)
            alertDialogFragment.isCancelable = false
            alertDialogFragment.setDialogListener(this)
            alertDialogFragment.show(context.supportFragmentManager, "UpdateFragment")
        }
    }

    override fun onClickPositiveButton(bundle: Bundle?, tag: String?)
    {
        if ("UpdateFragment".equals(tag!!, ignoreCase = true))
        {
            AsyncDownloadTask(context).execute(remoteUrl)
            sharedPreferences!!.edit().putString(CommonConstants.COMMIT_SHA_KEY, bundle!!.getString(CommonConstants.COMMIT_SHA_KEY)).apply()
        } else
        {
            this@HttpAsyncTask.onClickNegativeButton()
        }
    }

    override fun onClickNegativeButton()
    {
        context.finish()
    }

    companion object
    {
        private val CLASS_NAME = HttpAsyncTask::class.java.simpleName
        val REQUEST_METHOD = "GET"
        val READ_TIMEOUT = 15000
        val CONNECTION_TIMEOUT = 15000
    }
}
