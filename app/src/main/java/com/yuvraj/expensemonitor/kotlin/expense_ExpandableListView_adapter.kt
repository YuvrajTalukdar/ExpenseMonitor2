package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler

class expense_ExpandableListView_adapter(context1: Context, dataHandler1: data_handler, listener1: expense_list_adapter_listener): BaseExpandableListAdapter() {

    var context = context1
    var dataHandler=dataHandler1
    var listener2=listener1

    interface expense_list_adapter_listener {
        fun onDeleteButtonPressed(groupId: Int,childId: Int)
        fun onChildClicked(groupId:Int,childId: Int)
    }

    override fun getGroupCount(): Int {
        return dataHandler.expense_data_list.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return dataHandler.expense_data_list.get(groupPosition).item_data_list.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return dataHandler.expense_data_list.get(groupPosition)
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return dataHandler.expense_data_list.get(groupPosition).item_data_list.get(childPosition)
    }

    override fun getGroupId(groupPosition: Int): Long {
        return dataHandler.expense_data_list.get(groupPosition).id.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return dataHandler.expense_data_list.get(groupPosition).item_data_list.get(childPosition).item_id.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        //var group: String = getGroup(groupPosition) as String
        var view: View
        if(convertView == null)
        {
            var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.expandablelistview_group,null) as View
        }
        else
        {   view = convertView}

        //view.animation=AnimationUtils.loadAnimation(context,R.anim.item_animation_fall_down)

        var dateTextView: TextView = view.findViewById(R.id.date_textView)
        var amountTextView: TextView = view.findViewById(R.id.amountTextView)

        dateTextView.setText(""+dataHandler.expense_data_list.get(groupPosition).day+"/"+dataHandler.expense_data_list.get(groupPosition).month+"/"+dataHandler.expense_data_list.get(groupPosition).year)
        amountTextView.setText(dataHandler.expense_data_list.get(groupPosition).cost.toString())

        return view
    }

    override fun getChildView(groupPosition: Int, childPOsition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var view: View
        if(convertView == null)
        {
            var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.expandablelistview_item,null) as View
        }
        else
        {   view = convertView}

        //view.animation=AnimationUtils.loadAnimation(context,R.anim.item_animation_fall_down)

        var itemNameTextView: TextView = view.findViewById(R.id.item_name_textView)
        var itemCostTextView: TextView = view.findViewById(R.id.item_costTextView)
        var deleteButton: ImageButton = view.findViewById(R.id.expenseItemDeleteButton)
        //var itemCategoryTextView: TextView = view.findViewById(R.id.categoryTextView)

        itemNameTextView.setText(dataHandler.expense_data_list.get(groupPosition).item_data_list.get(childPOsition).item_name)
        itemCostTextView.setText(dataHandler.expense_data_list.get(groupPosition).item_data_list.get(childPOsition).item_cost.toString())
        deleteButton.setOnClickListener{
            listener2.onDeleteButtonPressed(groupPosition,childPOsition)
        }
        //itemCategoryTextView.setText(dataHandler.expense_data_list.get(groupPosition).item_data_list.get(childPOsition).category)
        view.isClickable=true
        view.setOnClickListener{
            listener2.onChildClicked(groupPosition,childPOsition)
        }
        return view
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}