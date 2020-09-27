package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.yuvraj.expensemonitor.R

class SyncFragment(var sign_in_handler: googleSignInHandler) : Fragment() {

    var is_signed_in=false
    var is_locked=false
    lateinit var email_textView: TextView
    internal lateinit var listener: sync_fragment_listener//= context as sync_fragment_listener
    lateinit var signInGoogleButton: Button
    lateinit var syncButton: Button
    lateinit var backupRestoreButton: Button
    lateinit var backupButton: Button
    lateinit var switch: Switch

    interface sync_fragment_listener
    {
        fun is_signed_in_status(): Boolean
        fun get_sign_in_status(): GoogleSignInAccount?
        fun backup_restore(start_code: Int)
        fun sync_now(first_time_sync: Boolean)
        fun check_auto_sync_state(): Boolean
        fun set_auto_sync_state(state: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v: View = inflater.inflate(R.layout.fragment_sync, container, false)

        email_textView = v.findViewById(R.id.account_name_TextView)
        signInGoogleButton = v.findViewById(R.id.Sign_In_With_Google_Account)
        syncButton = v.findViewById(R.id.sync_now_button)
        backupRestoreButton = v.findViewById(R.id.Load_Backup_file)
        backupButton = v.findViewById(R.id.Perform_Local_Backup)
        switch = v.findViewById(R.id.Auto_sync_switch)

        switch.setOnClickListener{
            if(listener.check_auto_sync_state())
            {   listener.set_auto_sync_state(false)}
            else
            {   listener.set_auto_sync_state(true)}
        }
        if(listener.check_auto_sync_state())
        {   switch.isChecked=true}
        else
        {   switch.isChecked=false}

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
        syncButton.setOnClickListener {
            listener.sync_now(false)
        }
        signed_in_layout(listener.is_signed_in_status())
        lock_ui(is_locked)

        return v
    }

    fun lock_ui(lock: Boolean)
    {
        is_locked=lock
        if(context!=null)
        {
            if (lock) {
                signInGoogleButton.isEnabled = false
                syncButton.isEnabled = false
                syncButton.setText(resources.getText(R.string.syncing))
                backupButton.isEnabled = false
                backupButton.isEnabled = false
                backupRestoreButton.isEnabled = false
                switch.isEnabled = false
            } else {
                signInGoogleButton.isEnabled = true
                syncButton.isEnabled = true
                syncButton.setText(resources.getText(R.string.sync_button_text))
                backupButton.isEnabled = true
                backupButton.isEnabled = true
                backupRestoreButton.isEnabled = true
                switch.isEnabled = true
            }
        }
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