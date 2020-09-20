package com.yuvraj.expensemonitor.kotlin

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.database_handler
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class add_expense_dialog() : DialogFragment(), DatePickerDialog.OnDateSetListener,
                                                AdapterView.OnItemSelectedListener{

    internal lateinit var listener: add_expense_listener
    var spinnerOption=0

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog
    {
        return activity?.let {

            var builder=AlertDialog.Builder(requireActivity(), R.style.CustomDialogTheme)
            var inflater=requireActivity().layoutInflater
            var view=inflater.inflate(R.layout.add_new_expense_dialog, null)
            builder.setView(view)

            var cost_editText: EditText = view.findViewById(R.id.item_cost_edittext)
            var itemNameEditText: EditText = view.findViewById(R.id.item_name_edittext)
            var datePickerTextView: TextView = view.findViewById(R.id.date_picket_textView)
            var spinner: Spinner = view.findViewById(R.id.select_category_spinner)

            var db= database_handler(context)
            var dataHandler=db._category_list
            var category_list = ArrayList<String>()
            category_list.clear()
            var a=0
            category_list.add("Select a category")
            while(a<dataHandler.category_data_list.size)
            {
                category_list.add(dataHandler.category_data_list.get(a).category_name)
                a++
            }
            dataHandler.category_data_list.clear()
            dataHandler.expense_data_list.clear()
            var categorySpinnerAdapter = ArrayAdapter(requireContext(),R.layout.spinner_color_layout,category_list)
            categorySpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
            spinner.adapter=categorySpinnerAdapter
            spinner.onItemSelectedListener=this

            var day=-1
            var month=-1
            var year=-1
            datePickerTextView.setOnClickListener()
            {
                var c=Calendar.getInstance()
                var datePickerDialog = DatePickerDialog(
                    requireContext(), R.style.DatePickerTheme,
                    { datePicker: DatePicker, year1: Int, month1: Int, day1: Int ->
                        datePickerTextView.setText("" + day1 + "/" + month1 + "/" + year1)
                        day = day1
                        month = month1
                        year = year1
                        //execure the code here
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }

            var ok_button: Button=view.findViewById(R.id.expense_dialog_ok)
            ok_button.setOnClickListener {
                var cost: Float= 0.0F
                try{
                    cost = cost_editText.text.toString().toFloat()
                }
                catch(e: Exception)
                {   e.printStackTrace()}

                listener.ExpenseDialogOnOKClicked(
                    0,
                    itemNameEditText.text.toString(),
                    cost,
                    category_list.get(spinnerOption),
                    day,
                    month,
                    year
                )
            }

            var cancel_button: Button=view.findViewById(R.id.expense_dialog_cancel)
            cancel_button.setOnClickListener{
                dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
        var map = listener.get_color_id()
        if(i>0)
        {   (adapterView!!.getChildAt(0) as TextView).setTextColor(map["MediumColor"]!!)}
        else
        {   (adapterView!!.getChildAt(0) as TextView).setTextColor(map["DeepColor"]!!)}
        spinnerOption=i
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
    }

    interface add_expense_listener
    {
        fun ExpenseDialogOnOKClicked(
            dialog_code: Int,
            item_name: String,
            item_cost: Float,
            item_category: String,
            dat: Int,
            month: Int,
            year: Int
        )
        fun get_color_id(): HashMap<String, Int>
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try
        {   listener = context as add_expense_listener}
        catch (e: ClassCastException)
        {   throw ClassCastException((context.toString() + " must implement add_expense_listener"))}
    }

}