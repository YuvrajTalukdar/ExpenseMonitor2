package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler
import com.yuvraj.expensemonitor.java.database_handler

class ExpensesFragment : Fragment() {

    lateinit var addExpenseDialog: add_expense_dialog
    lateinit var expandablelistviewAdapter: expense_ExpandableListView_adapter
    lateinit var db: database_handler
    lateinit var dataHandler: data_handler
    lateinit var mainActivity: MainActivity
    var editDialogGroupId=-1
    var editDialogChildId=-1
    var started_for_editing_data=false
    lateinit var statusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view=inflater.inflate(R.layout.fragment_expenses, container, false)

        addExpenseDialog= add_expense_dialog()
        var floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton_expense_fragment)
        floatingActionButton.setOnClickListener()
        {
            addExpenseDialog.dialogOption=0
            addExpenseDialog.show(parentFragmentManager, "add_expense_dialog")
        }

        db= database_handler(context)
        dataHandler = db._expense_data

        val listener=object:expense_ExpandableListView_adapter.expense_list_adapter_listener{
            override fun onDeleteButtonPressed(groupId: Int, childId: Int) {
                //delete data dialog
                mainActivity= activity as MainActivity

                var map= mainActivity.get_color_id()
                var deep_color = String.format("#%06X", 0xFFFFFF and map["DeepColor"]!!)
                var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
                map.clear()

                val materialAlertDialogBuilder= MaterialAlertDialogBuilder(activity as MainActivity,R.style.AlertDialogTheme)
                materialAlertDialogBuilder.setTitle(HtmlCompat.fromHtml("<font color="+medium_color+">"+resources.getString(R.string.delete_item)+"</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                materialAlertDialogBuilder.setMessage(
                    HtmlCompat.fromHtml("<font color="+deep_color+">"+resources.getString(R.string.delete_expense_dialog_content)+"</font>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
                materialAlertDialogBuilder.background = (activity as MainActivity).getDrawable(R.drawable.readymade_dialog_background)
                materialAlertDialogBuilder.setPositiveButton(
                    HtmlCompat.fromHtml("<font color="+medium_color+">Yes</font>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY)){
                        dialogInterface,which->
                    run{
                        deleteExpenseData(groupId,childId)
                        Toast.makeText(context, "Item Removed.", Toast.LENGTH_SHORT).show()
                    }
                }
                materialAlertDialogBuilder.setNegativeButton(
                    HtmlCompat.fromHtml("<font color="+medium_color+">No</font>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY)){
                        dialog,which->
                    run {
                        //Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
                    }
                }
                materialAlertDialogBuilder.show()
            }
            override fun onChildClicked(groupId: Int, childId: Int)
            {
                addExpenseDialog.dialogOption=1
                addExpenseDialog.item_name=dataHandler.expense_data_list.get(groupId).item_data_list.get(childId).item_name
                addExpenseDialog.cost=dataHandler.expense_data_list.get(groupId).item_data_list.get(childId).item_cost
                addExpenseDialog.day2=dataHandler.expense_data_list.get(groupId).day
                addExpenseDialog.month2=dataHandler.expense_data_list.get(groupId).month
                addExpenseDialog.year2=dataHandler.expense_data_list.get(groupId).year
                addExpenseDialog.category_name2=dataHandler.expense_data_list.get(groupId).item_data_list.get(childId).category
                editDialogGroupId=groupId
                editDialogChildId=childId
                started_for_editing_data=true
                addExpenseDialog.show(parentFragmentManager, "add_expense_dialog")
            }
        }
        var expandableListView: ExpandableListView = view.findViewById(R.id.expense_list_expandable_list_view)
        expandablelistviewAdapter = expense_ExpandableListView_adapter(
            requireContext(),
            dataHandler,
            listener
        )
        expandableListView.setAdapter(expandablelistviewAdapter)
        expandablelistviewAdapter.notifyDataSetChanged()
        //expandableListView.animation=AnimationUtils.loadAnimation(context,R.anim.item_animation_fall_down)
        expandableListView.setOnGroupClickListener { parent, v, groupPosition, id ->
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
        statusTextView=view.findViewById(R.id.status_textView)
        if(dataHandler.expense_data_list.size!=0)
        {   statusTextView.visibility=View.GONE}
        else
        {   statusTextView.visibility=View.VISIBLE}

        return view
    }

    //expense database functions
    private fun get_month_size_in_days(month: Int, year: Int): Int {
        val feb_size: Int = if (month % 2 == 0) {
            if (year % 4 == 0 && year % 400 != 0 && year % 100 == 0) {
                28
            } else if (year % 4 == 0) {
                29
            } else {
                28
            }
        } else {
            31
        }
        var days = 0
        for (a in 1 until month) {
            days += if (a == 2) {
                feb_size
            } else if (a % 2 == 0) {
                30
            } else {
                31
            }
        }
        return days
    }

    fun get_year_size_in_days(year: Int): Int
    {
        if(year%4==0 && year%400!=0 && year%100==0)
        {   return 365}
        else if(year%4==0)
        {   return 366}
        else
        {   return 365}
    }

    fun deleteExpenseData(groupId: Int, childId: Int)
    {
        db.delete_expense_data(dataHandler.expense_data_list.get(groupId).item_data_list.get(childId).item_id)
        dataHandler.expense_data_list.get(groupId).cost-=dataHandler.expense_data_list.get(groupId).item_data_list.get(childId).item_cost
        dataHandler.expense_data_list.get(groupId).item_data_list.removeAt(childId)
        if(dataHandler.expense_data_list.get(groupId).item_data_list.size==0)
        {   dataHandler.expense_data_list.removeAt(groupId)}
        expandablelistviewAdapter.notifyDataSetChanged()
        if(dataHandler.expense_data_list.size==0)
        {   statusTextView.visibility=View.VISIBLE}
    }

    fun addExpenseData(item_name: String, item_cost: Float, item_category: String, day: Int, month: Int, year: Int)
    {
        //data validity checkers
        if(item_cost==0F)
        {   Toast.makeText(activity, "Enter cost.", Toast.LENGTH_SHORT).show()}
        else if(item_name.isEmpty())
        {   Toast.makeText(activity, "Enter item name.", Toast.LENGTH_SHORT).show()}
        else if(item_category.equals("Select a category"))
        {   Toast.makeText(activity, "Specify item category.", Toast.LENGTH_SHORT).show()}
        else
        {
            var toast_string=""
            if(started_for_editing_data)
            {
                deleteExpenseData(editDialogGroupId,editDialogChildId)
                started_for_editing_data=false
                toast_string="Expense Edited."
            }
            else
            {   toast_string="Expense Added."}

            db.add_expense_data(item_name, item_cost, item_category, day, month, year)
            var a=0;
            var no_hit=false
            if(dataHandler.expense_data_list.size>0)
            {
                while((year*get_year_size_in_days(year)+get_month_size_in_days(month, year)+day)<(
                            dataHandler.expense_data_list.get(a).year*get_year_size_in_days(dataHandler.expense_data_list.get(a).year)+
                                    get_month_size_in_days(dataHandler.expense_data_list.get(a).month, dataHandler.expense_data_list.get(a).year)+
                                    dataHandler.expense_data_list.get(a).day))
                {
                    a++
                    if(dataHandler.expense_data_list.size<=a)
                    {
                        no_hit=true
                        break
                    }
                }
            }
            var item = data_handler.item_data()
            item.item_name=item_name
            item.category=item_category
            item.item_cost=item_cost
            item.item_id=db._last_entered_expense_data_id
            if(!no_hit && dataHandler.expense_data_list.size>0 && dataHandler.expense_data_list.get(a).day==day && dataHandler.expense_data_list.get(a).month==month && dataHandler.expense_data_list.get(a).year==year)
            {
                dataHandler.expense_data_list.get(a).item_data_list.add(item)
                dataHandler.expense_data_list.get(a).cost+=item_cost
            }
            else
            {
                var data = data_handler.expense_data_handler()
                data.cost=item_cost
                data.day=day
                data.month=month
                data.year=year
                data.id=dataHandler.expense_data_list.size+1
                data.item_data_list.add(item)
                data.item_data_list.get(0).item_cost=item_cost
                dataHandler.expense_data_list.add(a, data)
            }
            addExpenseDialog.dismiss()
            expandablelistviewAdapter.notifyDataSetChanged()
            if(dataHandler.expense_data_list.size>0)
            {   statusTextView.visibility=View.GONE}

            Toast.makeText(context, toast_string, Toast.LENGTH_SHORT).show()
        }
    }
}