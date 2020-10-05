package com.yuvraj.expensemonitor2.kotlin

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.yuvraj.expensemonitor2.R

class add_category_dialog() : DialogFragment() {

    lateinit var category_name_editText: EditText
    internal lateinit var listener: add_category_listener
    var dialogCode=0

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog
    {
        return activity?.let {

            var builder=AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            var inflater=requireActivity().layoutInflater
            var view=inflater.inflate(R.layout.add_rename_category_dialog, null)
            builder.setView(view)
            var headingTextView: TextView=view.findViewById(R.id.add_rename_headingTextView)
            if(dialogCode==0)
            {   headingTextView.setText(resources.getText(R.string.category_dialog_title))}
            else if(dialogCode==1)
            {   headingTextView.setText(resources.getText(R.string.rename_category))}

            category_name_editText=view.findViewById(R.id.category_name_edittext)

            var ok_button: Button=view.findViewById(R.id.category_dialog_ok)
            ok_button.setOnClickListener(View.OnClickListener {
                listener.CategoryDialogOnOKClicked(dialogCode,category_name_editText.text.toString())
            })

            var cancel_button: Button=view.findViewById(R.id.category_dialog_cancel)
            cancel_button.setOnClickListener(View.OnClickListener {
                dismiss()
            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    interface add_category_listener
    {   fun CategoryDialogOnOKClicked(dialog_code: Int,category_name: String)}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try
        {   listener = context as add_category_listener}
        catch (e: ClassCastException)
        {   throw ClassCastException((context.toString() + " must implement add_category_listener"))}
    }

}