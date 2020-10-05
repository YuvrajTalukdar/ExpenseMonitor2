package com.yuvraj.expensemonitor2.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.yuvraj.expensemonitor2.R
import com.yuvraj.expensemonitor2.java.data_handler

class category_list_recycler_view_adapter(dataHandler1: data_handler,listener1: category_list_adapter_listener): RecyclerView.Adapter<category_list_recycler_view_adapter.data_holder>(){

    var dataHandler=dataHandler1
    var listener2=listener1

    interface category_list_adapter_listener {
        fun onDeleteButtonPressed(data_id: Int,adapter_position: Int)
        fun onCategoryItemPressed(data_id:Int,adapter_position: Int)
    }

    class data_holder(@NonNull itemView: View, listener: category_list_adapter_listener,dataHandler: data_handler) : RecyclerView.ViewHolder(itemView) {
        var categoryItemTextView: TextView=itemView.findViewById(R.id.category_item_textView)
        var deleteButton: ImageButton=itemView.findViewById(R.id.category_item_delete_button)

        init
        {
            categoryItemTextView.setOnClickListener{
                if(dataHandler.category_data_list.get(adapterPosition).id!=1)
                {   listener.onCategoryItemPressed(dataHandler.category_data_list.get(adapterPosition).id,adapterPosition)}
            }
            deleteButton.setOnClickListener {
                listener.onDeleteButtonPressed(dataHandler.category_data_list.get(adapterPosition).id,adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): data_holder {
        var v: View=LayoutInflater.from(parent.context).inflate(R.layout.category_recycler_view_item,parent,false)
        return data_holder(v,listener2,dataHandler)
    }

    override fun onBindViewHolder(holder: data_holder, position: Int) {
        holder.categoryItemTextView.setText(dataHandler.category_data_list.get(position).category_name)
        when (dataHandler.category_data_list.get(position).id) {
            1 -> {   holder.deleteButton.isVisible=false}
            else -> {   holder.deleteButton.isVisible=true}
        }
    }

    override fun getItemCount(): Int {  return dataHandler.category_data_list.size}
}