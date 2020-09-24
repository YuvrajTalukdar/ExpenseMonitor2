package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.DriveServiceHelper

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
        fun start_account_choosing_activity(i: Intent,code: Int)
    }
    //sign in functions
    fun signInWithGoogle()
    {
        mGoogleSignInClient = buildGoogleSignInClient()
        listener.start_account_choosing_activity(mGoogleSignInClient.signInIntent,REQUEST_CODE_SIGN_IN)
    }
    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context1, signInOptions)
    }
    fun sign_in_handler(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
            is_signed_in = true
            listener.set_sign_in_state(true)
            initialize_google_drive_service_helper()
            Toast.makeText(context1, "Sign in complete.", Toast.LENGTH_SHORT).show()
            //sync_now(true)
        }.addOnFailureListener { e ->
            println("Failed to sign in. Cause: ")
            e.printStackTrace()
        }
    }
    private fun initialize_google_drive_service_helper() {
        if (is_signed_in) {
            val account = GoogleSignIn.getLastSignedInAccount(context1)
            val credential = GoogleAccountCredential.usingOAuth2(context1, setOf(DriveScopes.DRIVE_FILE))
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
        materialAlertDialogBuilder.setTitle(HtmlCompat.fromHtml("<font color=$medium_color>Sign Out</font>",HtmlCompat.FROM_HTML_MODE_LEGACY))
        materialAlertDialogBuilder.setMessage(HtmlCompat.fromHtml("<font color=$deep_color>Are you sure you want to sign out?</font>",HtmlCompat.FROM_HTML_MODE_LEGACY))
        materialAlertDialogBuilder.background = context1.getDrawable(R.drawable.readymade_dialog_background)
        materialAlertDialogBuilder.setPositiveButton(
            HtmlCompat.fromHtml("<font color=$medium_color>Yes</font>", HtmlCompat.FROM_HTML_MODE_LEGACY)
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
            HtmlCompat.fromHtml("<font color=$medium_color>No</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
        ) { dialogInterface, i -> }
        materialAlertDialogBuilder.show()
    }
}