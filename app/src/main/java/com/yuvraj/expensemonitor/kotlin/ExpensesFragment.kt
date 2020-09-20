package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yuvraj.expensemonitor.R

class ExpensesFragment : Fragment() {

    lateinit var addExpenseDialog: add_expense_dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view=inflater.inflate(R.layout.fragment_expenses, container, false)

        addExpenseDialog= add_expense_dialog()
        var floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton_expense_fragment)
        floatingActionButton.setOnClickListener()
        {
            addExpenseDialog.show(parentFragmentManager,"add_expense_dialog")
        }

        return view
    }

    fun addExpenseData()
    {

    }
}