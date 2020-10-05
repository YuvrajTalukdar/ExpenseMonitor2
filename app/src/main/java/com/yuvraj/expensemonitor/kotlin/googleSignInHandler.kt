package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.documentfile.provider.DocumentFile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.DriveServiceHelper
import com.yuvraj.expensemonitor.java.database_handler
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class googleSignInHandler(var context1: Context)  {

    var is_signed_in=false
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mDriveServiceHelper: DriveServiceHelper
    private val REQUEST_CODE_SIGN_IN = 100
    internal var listener=context1 as googleSignInHandler_listener

    interface googleSignInHandler_listener
    {
        fun load_account_image_and_id(account: GoogleSignInAccount)
        fun set_sign_in_state(state: Boolean)
        fun get_color_id(): HashMap<String, Int>
        fun reset_account_image_and_id()
        fun start_account_choosing_activity(i: Intent, code: Int)
        fun check_internet_connection(): Boolean
        fun get_sign_in_status(): GoogleSignInAccount?
        fun lock_ui(lock: Boolean)
    }
    //sign in functions
    fun signInWithGoogle()
    {
        mGoogleSignInClient = buildGoogleSignInClient()
        listener.start_account_choosing_activity(
            mGoogleSignInClient.signInIntent,
            REQUEST_CODE_SIGN_IN
        )
    }
    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context1, signInOptions)
    }
    fun sign_in_handler(data: Intent?,dbHandler: database_handler) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            is_signed_in = true
            listener.set_sign_in_state(true)
            initialize_google_drive_service_helper()
            Toast.makeText(context1, "Sign in complete.", Toast.LENGTH_SHORT).show()
            sync_now(true,dbHandler)
        }.addOnFailureListener { e ->
            println("Failed to sign in. Cause: ")
            e.printStackTrace()
        }
    }
    private fun initialize_google_drive_service_helper() {
        if (is_signed_in) {
            val account = GoogleSignIn.getLastSignedInAccount(context1)
            val credential = GoogleAccountCredential.usingOAuth2(
                context1,
                setOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account!!.account
            val googleDriveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("AppName").build()
            mDriveServiceHelper = DriveServiceHelper(googleDriveService)
            listener.load_account_image_and_id(account)
        }
    }

    fun get_email(): String
    {   return GoogleSignIn.getLastSignedInAccount(context1)!!.email.toString() }

    //sign out function
    fun sign_out() {
        val account_name = GoogleSignIn.getLastSignedInAccount(context1)!!
            .email
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context1)
        val map: MutableMap<String, Int> = listener.get_color_id()
        val deep_color = String.format("#%06X", 0xFFFFFF and map["DeepColor"]!!)
        val medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
        map.clear()
        materialAlertDialogBuilder.setTitle(
            HtmlCompat.fromHtml(
                "<font color=$medium_color>Sign Out</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
        materialAlertDialogBuilder.setMessage(
            HtmlCompat.fromHtml(
                "<font color=$deep_color>Are you sure you want to sign out?</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        )
        materialAlertDialogBuilder.background = context1.getDrawable(R.drawable.readymade_dialog_background)
        materialAlertDialogBuilder.setPositiveButton(
            HtmlCompat.fromHtml(
                "<font color=$medium_color>Yes</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        ) { dialogInterface, i ->
            mGoogleSignInClient = buildGoogleSignInClient()
            mGoogleSignInClient.signOut().addOnSuccessListener {
                println("Sign out successful.")
                is_signed_in = false
                listener.set_sign_in_state(false)
            }.addOnFailureListener { e -> println("Sign out failed. Cause: " + e.stackTrace) }
            Toast.makeText(context1, "Signed Out of $account_name", Toast.LENGTH_SHORT).show()
            listener.reset_account_image_and_id()
        }
        materialAlertDialogBuilder.setNegativeButton(
            HtmlCompat.fromHtml(
                "<font color=$medium_color>No</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        ) { dialogInterface, i -> }
        materialAlertDialogBuilder.show()
    }

    private fun isOnline(): Task<Boolean> {
        val mExecutor: Executor = Executors.newSingleThreadExecutor()
        return Tasks.call(mExecutor, {
            try {
                val timeoutMs = 1500
                val sock = Socket()
                val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                sock.connect(sockaddr, timeoutMs)
                sock.close()
                true
            } catch (e: IOException) {
                false
            }
        })
    }
    private fun sync_upload(dbHandler: database_handler)
    {
        try{
            var file = java.io.File(context1.filesDir.path + "/" + "ExpenseMonitorBackup")
            var documentFile = DocumentFile.fromFile(file)
            var outputStream = context1.contentResolver.openOutputStream(documentFile.uri)
            dbHandler.backup_data(outputStream)
            mDriveServiceHelper.uploadFile(documentFile.name, file).addOnCompleteListener{
                Toast.makeText(context1, "Sync complete.", Toast.LENGTH_SHORT).show()
                listener.lock_ui(false)
            }
        }
        catch (e: Exception)
        {   e.printStackTrace()}
    }
    private fun sync_download(dbHandler: database_handler)
    {
        try{
            mDriveServiceHelper.queryFiles().addOnCompleteListener{
                var file_list: List<File> = it.getResult()!!.files
                var a=0;
                var found=false
                while(a<file_list.size)
                {
                    if(file_list.get(a).name.contains("ExpenseMonitorBackup"))
                    {
                        found=true
                        var data_file = java.io.File(context1.filesDir.path + "/" + file_list.get(a).name)
                        try{
                            data_file.createNewFile()
                            mDriveServiceHelper.downloadFile(data_file, file_list.get(a).id).addOnCompleteListener{
                                if(it.getResult()==true)
                                {
                                    var file = DocumentFile.fromFile(data_file)
                                    var inputStream=context1.contentResolver.openInputStream(file.uri)
                                    dbHandler.restore_data(inputStream).addOnCompleteListener{
                                        data_file.delete()
                                        mDriveServiceHelper.delete_backup_file().addOnCompleteListener{
                                            sync_upload(dbHandler)
                                        }
                                    }
                                }
                            }
                        }
                        catch (e: Exception)
                        {   e.printStackTrace()}
                        break
                    }
                    a++
                }
                if(!found)
                {   sync_upload(dbHandler)}
            }
        }
        catch (e: Exception)
        {   e.printStackTrace()}
    }
    fun sync_now(first_time_sync: Boolean, dbHandler: database_handler)
    {
        var is_online = false
        var account=listener.get_sign_in_status()
        if(account==null)
        {   is_signed_in=false}
        else
        {   is_signed_in=true}
        if(listener.check_internet_connection())
        {
            isOnline().addOnCompleteListener{
                is_online = it.result!!
                if(!::mDriveServiceHelper.isInitialized && is_online)
                {   initialize_google_drive_service_helper()}
                if(!is_online)
                {   Toast.makeText(context1, "Please check your internet connection.", Toast.LENGTH_SHORT).show()}
                else if(!is_signed_in)
                {   Toast.makeText(context1, "Please sign in with google first.", Toast.LENGTH_SHORT).show()}
                else if(is_online && is_signed_in)
                {
                    listener.lock_ui(true)
                    if(first_time_sync)
                    {   sync_download(dbHandler)}
                    else
                    {
                        mDriveServiceHelper.delete_backup_file().addOnCompleteListener{
                            sync_upload(dbHandler)
                        }
                    }
                }
            }
        }
    }
}