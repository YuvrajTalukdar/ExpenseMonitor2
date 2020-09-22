package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler

class ExpensesFragment : Fragment() {

    lateinit var addExpenseDialog: add_expense_dialog
    lateinit var expandablelistviewAdapter: expense_ExpandableListView_adapter

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

        var dataHandler: data_handler = data_handler()
        dataHandler.expense_data_list.clear()
        var a=0
        var b=0
        while(a<5)
        {
            var group = data_handler.expense_data_handler()
            group.id=a
            group.cost=40.1.toFloat()
            group.date=""+a+1+"/02/2020"
            b=0
            while(b<3)
            {
                var item = data_handler.item_data()
                item.item_id=b
                item.item_cost=10+2*b.toFloat()
                item.category="category"+b
                item.item_name="name_"+b
                group.item_data_list.add(item)
                b++
            }
            dataHandler.expense_data_list.add(group)
            a++
        }
        val listener=object:expense_ExpandableListView_adapter.expense_list_adapter_listener{
            override fun onDeleteButtonPressed(groupId: Int,childId: Int) {
                //delete data dialog
                //remove data from database
                dataHandler.expense_data_list.get(groupId).item_data_list.removeAt(childId)
                if(dataHandler.expense_data_list.get(groupId).item_data_list.size==0)
                {   dataHandler.expense_data_list.removeAt(groupId)}
                expandablelistviewAdapter.notifyDataSetChanged()
            }
            override fun onChileClicked(groupId:Int,childId: Int)
            {
                println("group= "+groupId+" child= "+childId)
                //edit item dialog
            }
        }
        var expandableListView: ExpandableListView = view.findViewById(R.id.expense_list_expandable_list_view)
        expandablelistviewAdapter = expense_ExpandableListView_adapter(requireContext(),dataHandler,listener)
        expandableListView.setAdapter(expandablelistviewAdapter)
        expandablelistviewAdapter.notifyDataSetChanged()
        //expandableListView.animation=AnimationUtils.loadAnimation(context,R.anim.item_animation_fall_down)
        expandableListView.setOnGroupClickListener {
            parent, v, groupPosition, id ->
            run {
                var arrow: ImageView = v.findViewById(R.id.arrow_imageView2)
                if(!expandableListView.isGroupExpanded(groupPosition))
                {
                    expandableListView.expandGroup(groupPosition, true)
                    arrow.rotation= 180F
                }
                else
                {
                    expandableListView.collapseGroup(groupPosition)
                    arrow.rotation= 0F
                }
            }
            true
        }
        return view
    }

    fun addExpenseData()
    {

    }
}