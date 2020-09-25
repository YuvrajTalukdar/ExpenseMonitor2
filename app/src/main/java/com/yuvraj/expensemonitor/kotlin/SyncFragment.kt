package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.database_handler

class SyncFragment(var sign_in_handler: googleSignInHandler) : Fragment() {

    var is_signed_in=false
    lateinit var email_textView: TextView
    internal lateinit var listener: sync_fragment_listener//= context as sync_fragment_listener
    lateinit var signInGoogleButton: Button

    interface sync_fragment_listener
    {
        fun is_signed_in_status(): Boolean
        fun get_sign_in_status(): GoogleSignInAccount?
        fun backup_restore(start_code: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v: View = inflater.inflate(R.layout.fragment_sync, container, false)

        email_textView = v.findViewById(R.id.account_name_TextView)
        signInGoogleButton = v.findViewById(R.id.Sign_In_With_Google_Account)
        var syncButtin: Button = v.findViewById(R.id.sync_now_button)
        var backupRestoreButton: Button = v.findViewById(R.id.Load_Backup_file)
        var backupButton: Button = v.findViewById(R.id.Perform_Local_Backup)
        var seitch: Switch = v.findViewById(R.id.Auto_sync_switch)

        backupRestoreButton.setOnClickListener{
            listener.backup_restore(0)
        }
        backupButton.setOnClickListener{
            listener.backup_restore(1)
        }
        signInGoogleButton.setOnClickListener {
            if (!is_signed_in) {
                sign_in_handler.signInWithGoogle()
            } else {
                sign_in_handler.sign_out()
            }
        }
        syncButtin.setOnClickListener {

        }
        signed_in_layout(listener.is_signed_in_status())

        return v
    }

    fun signed_in_layout(status: Boolean) {
        is_signed_in = status
        if (context != null)
        {
            if (status) {
                email_textView.setText(sign_in_handler.get_email())
                signInGoogleButton.setText(resources.getString(R.string.sign_out_button_text))
            } else {
                email_textView.setText(resources.getString(R.string.not_signed_in))
                signInGoogleButton.setText(resources.getString(R.string.sign_in_button_text))
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try
        {   listener = context as sync_fragment_listener
        }
        catch (e: ClassCastException)
        {   throw ClassCastException((context.toString() + " must implement sync_fragment_listener"))}
    }
}