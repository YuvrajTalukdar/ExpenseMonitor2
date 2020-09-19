package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler
import com.yuvraj.expensemonitor.java.database_handler

class CategoryFragment() : Fragment(){

    lateinit var add_category_dialog: add_category_dialog
    lateinit var recycler_view: RecyclerView
    lateinit var recyclerViewAdapter: category_list_recycler_view_adapter
    lateinit var mainActivity: MainActivity
    lateinit var db: database_handler

    var category_item_clicked_dataid=-1
    var category_item_clicked_adapter_position=-1
    var dataHandler: data_handler=data_handler()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view=inflater.inflate(R.layout.fragment_category, container, false)

        add_category_dialog=add_category_dialog()
        var floating_button: FloatingActionButton=view.findViewById(R.id.floatingActionButton_category_fragment)
        floating_button.setOnClickListener()
        {
            add_category_dialog.dialogCode=0
            add_category_dialog.show(parentFragmentManager, "add_category_dialog")
        }

        recycler_view=view.findViewById(R.id.category_list_recyclerView)

        db= database_handler(context)
        dataHandler=db._category_list

        val listener=object:category_list_recycler_view_adapter.category_list_adapter_listener{
            override fun onDeleteButtonPressed(data_id: Int,adapter_position: Int) {
                mainActivity= activity as MainActivity

                var map= mainActivity.get_color_id()
                var deep_color = String.format("#%06X", 0xFFFFFF and map["DeepColor"]!!)
                var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
                map.clear()

                val materialAlertDialogBuilder= MaterialAlertDialogBuilder(activity as MainActivity,R.style.AlertDialogTheme)
                materialAlertDialogBuilder.setTitle(HtmlCompat.fromHtml("<font color="+medium_color+">"+resources.getString(R.string.delete_category)+"</font>", HtmlCompat.FROM_HTML_MODE_LEGACY));
                materialAlertDialogBuilder.setMessage(HtmlCompat.fromHtml("<font color="+deep_color+">"+resources.getString(R.string.delete_dialog_content)+"</font>",HtmlCompat.FROM_HTML_MODE_LEGACY));
                materialAlertDialogBuilder.background = (activity as MainActivity).getDrawable(R.drawable.readymade_dialog_background)
                materialAlertDialogBuilder.setPositiveButton(HtmlCompat.fromHtml("<font color="+medium_color+">Yes</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)){
                    dialogInterface,which->
                    run{
                        var a=0;
                        while(a<dataHandler.category_data_list.size)
                        {
                            if(dataHandler.category_data_list.get(a).id==data_id)
                            {
                                //remove data from database
                                db.remove_category(data_id)

                                dataHandler.category_data_list.removeAt(a)
                                recyclerViewAdapter.notifyItemRemoved(adapter_position)
                                break
                            }
                            a++
                        }
                        Toast.makeText(context, "Category removed.", Toast.LENGTH_SHORT).show()
                    }
                }
                materialAlertDialogBuilder.setNegativeButton(HtmlCompat.fromHtml("<font color="+medium_color+">No</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)){
                        dialog,which->
                    run {
                        //Toast.makeText(context, "No", Toast.LENGTH_SHORT).show()
                    }
                }
                materialAlertDialogBuilder.show()
            }

            override fun onCategoryItemPressed(data_id: Int,adapter_position: Int) {
                category_item_clicked_adapter_position=adapter_position
                category_item_clicked_dataid=data_id

                add_category_dialog= add_category_dialog()
                add_category_dialog.dialogCode=1
                add_category_dialog.show(parentFragmentManager,"rename_category_dialog")
            }
        }
        recyclerViewAdapter= category_list_recycler_view_adapter(dataHandler,listener)
        var linear_layout= LinearLayoutManager(view.context)
        recycler_view.adapter=recyclerViewAdapter
        recycler_view.layoutManager=linear_layout

        return view
    }

    fun rename_category(category_name: String)
    {
        if(category_name.isEmpty())
        {   Toast.makeText(activity,"Enter category name.", Toast.LENGTH_SHORT).show()}
        else
        {
            add_category_dialog.dismiss()
            //rename data in both category and expense table
            db.rename_category(category_item_clicked_dataid,category_name)

            dataHandler.category_data_list=db._category_list.category_data_list
            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    fun add_new_category(category_name: String)
    {
        if(category_name.isEmpty())
        {   Toast.makeText(activity,"Enter category name.", Toast.LENGTH_SHORT).show()}
        else
        {
            var a=0
            var found=false
            while(a<dataHandler.category_data_list.size)
            {
                if(dataHandler.category_data_list.get(a).category_name.equals(category_name))
                {
                    found=true
                    break;
                }
                a++
            }
            if(!found)
            {
                add_category_dialog.dismiss()
                //add data to database
                db.add_new_category(category_name)
                dataHandler.category_data_list = db._category_list.category_data_list//.add(data_handler.category_data_handler(dataHandler.category_data_list.size,category_name))
                recyclerViewAdapter.notifyDataSetChanged()
                recycler_view.scrollToPosition(dataHandler.category_data_list.size - 1)
            }
            else
            {   Toast.makeText(activity,"Category "+category_name+" already present.", Toast.LENGTH_SHORT).show()}
        }
    }
}