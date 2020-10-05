package com.yuvraj.expensemonitor.kotlin

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.yuvraj.expensemonitor.R

class aboutDialog: DialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            var builder=AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            var inflater=requireActivity().layoutInflater
            var view=inflater.inflate(R.layout.about_dialog, null)
            builder.setView(view)

            var imageView: ImageView = view.findViewById(R.id.appIconImageView)
            Glide.with(this).load(R.drawable.app_icon).into(imageView)

            var button: Button = view.findViewById(R.id.about_ok_button)
            button.setOnClickListener{
                dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}