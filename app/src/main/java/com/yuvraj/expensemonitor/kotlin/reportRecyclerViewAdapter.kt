package com.yuvraj.expensemonitor.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.yuvraj.expensemonitor.R
import java.util.HashMap

class reportRecyclerViewAdapter(var map: HashMap<String,Int>, var month_wise_data: ArrayList<ReportFragment.data_month_wise>, var mode_changing_data: ReportFragment.mode_changing_data_class): RecyclerView.Adapter<reportRecyclerViewAdapter.data_holder>(){

    var month_list = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    class data_holder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var month_category_text_view: TextView = itemView.findViewById(R.id.month_category_text_view)
        var cost_text_view: TextView = itemView.findViewById(R.id.cost_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): data_holder {
        var v: View= LayoutInflater.from(parent.context).inflate(R.layout.report_recycler_view_item,parent,false)
        return data_holder(v)
    }

    override fun onBindViewHolder(holder: data_holder, position: Int) {
        if(mode_changing_data.category_mode)//for category wise spending mode
        {
            if(mode_changing_data.index<month_wise_data.size) {
                if (position == 0)
                {
                    holder.month_category_text_view.setText("Total")
                    map["DeepColor"]?.let { holder.month_category_text_view.setTextColor(it) }
                    map["DeepColor"]?.let { holder.cost_text_view.setTextColor(it) }
                    holder.cost_text_view.setText(month_wise_data.get(mode_changing_data.index).total_month_expense.toString())
                }
                else
                {
                    map["MediumColor"]?.let { holder.month_category_text_view.setTextColor(it) }
                    map["MediumColor"]?.let { holder.cost_text_view.setTextColor(it) }
                    holder.month_category_text_view.setText(month_wise_data.get(mode_changing_data.index).category_wise_expense.get(position - 1).category)
                    holder.cost_text_view.setText(month_wise_data.get(mode_changing_data.index).category_wise_expense.get(position - 1).total_category_wise_expense.toString())
                }
            }
        }
        else// for monthly spending mode
        {
            map["MediumColor"]?.let { holder.month_category_text_view.setTextColor(it) }
            map["MediumColor"]?.let { holder.cost_text_view.setTextColor(it) }
            holder.month_category_text_view.setText(month_list.get(month_wise_data.get(position).month-1)+" "+month_wise_data.get(position).year)
            holder.cost_text_view.setText(month_wise_data.get(position).total_month_expense.toString())
        }
    }

    override fun getItemCount(): Int {
        if(mode_changing_data.category_mode)
        {
            if(mode_changing_data.index>=month_wise_data.size)//no  data in the current month condition
            {   return 0}
            else//for category wise data in a particular month
            {   return month_wise_data.get(mode_changing_data.index).category_wise_expense.size+1}
        }
        else
        {
            return month_wise_data.size
        }
        /*
        if(mode_changing_data.category_mode && mode_changing_data.index<month_wise_data.size && mode_changing_data.index!=-1)
        {   return month_wise_data.get(mode_changing_data.index).category_wise_expense.size+1}
        else if(mode_changing_data.index>=month_wise_data.size)
        {   return 0}
        else if(mode_changing_data.index==-1 || )
        {   return month_wise_data.size}*/
    }
}