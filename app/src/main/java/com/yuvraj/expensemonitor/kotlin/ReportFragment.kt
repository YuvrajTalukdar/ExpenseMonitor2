package com.yuvraj.expensemonitor.kotlin

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler
import com.yuvraj.expensemonitor.java.database_handler
import kotlinx.android.synthetic.main.fragment_report.*
import java.util.*
import kotlin.collections.ArrayList


class ReportFragment : Fragment(), AdapterView.OnItemSelectedListener{

    var month_list = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    lateinit var data: data_handler
    lateinit var db: database_handler
    lateinit var barChart: BarChart
    lateinit var pieChart: PieChart
    internal lateinit var listener: reportFragmentListener
    var year_list = ArrayList<Int>()
    var c=Calendar.getInstance()
    var month=c.get(Calendar.MONTH)
    var year=c.get(Calendar.YEAR)
    lateinit var recycler_view: RecyclerView
    lateinit var recyclerViewAdapter: reportRecyclerViewAdapter
    var month_index=0


    class data_month_wise{
        var month:Int = 0
        var year:Int = 0
        var total_month_expense=0.0f
        class data_category_wise{
            var category=""
            var total_category_wise_expense=0.0f
        }
        var category_wise_expense = ArrayList<data_category_wise>()
    }
    var month_wise_expense = ArrayList<data_month_wise>()
    class mode_changing_data_class{
        var year=0
        var month=0
        var index=0
        var category_mode=false
    }
    var mode_changing_data = mode_changing_data_class()

    interface reportFragmentListener
    {
        fun get_color_id(): HashMap<String, Int>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view=inflater.inflate(R.layout.fragment_report, container, false)

        listener = context as reportFragmentListener
        var category_wise_spending_button: Button = view.findViewById(R.id.category_wise_spending)
        var monthly_spending_button: Button = view.findViewById(R.id.monthly_spending)
        var month_spinner: Spinner = view.findViewById(R.id.month_spinner)
        var year_spinner: Spinner = view.findViewById(R.id.year_spinner)
        recycler_view = view.findViewById(R.id.report_recyclerView)

        barChart = view.findViewById(R.id.barChart)
        pieChart = view.findViewById(R.id.pieChart)

        var monthSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_color_layout2,
            month_list
        )
        monthSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        month_spinner.adapter=monthSpinnerAdapter
        month_spinner.onItemSelectedListener=this

        //data preparation part
        year_list.clear()
        month_wise_expense.clear()
        db = database_handler(context)
        data = db._expense_data
        var a=0
        var last_year=0
        var month_data = data_month_wise()
        while(a<data.expense_data_list.size)
        {
            //collecting the year data
            if(last_year!=data.expense_data_list.get(a).year)
            {
                last_year = data.expense_data_list.get(a).year
                year_list.add(last_year)
            }
            //collecting chart data
            if(month_data.month == data.expense_data_list.get(a).month && month_data.year == data.expense_data_list.get(a).year)
            {
                var b=0
                while(b<data.expense_data_list.get(a).item_data_list.size)
                {
                    month_data.total_month_expense+=data.expense_data_list.get(a).item_data_list.get(b).item_cost
                    var c=0
                    var found=false
                    while(c<month_data.category_wise_expense.size)
                    {
                        if(month_data.category_wise_expense.get(c).category.equals(data.expense_data_list.get(a).item_data_list.get(b).category))
                        {
                            month_data.category_wise_expense.get(c).total_category_wise_expense+=data.expense_data_list.get(a).item_data_list.get(b).item_cost
                            found=true
                            break
                        }
                        c++
                    }
                    if(!found)
                    {
                        var category = data_month_wise.data_category_wise()
                        category.total_category_wise_expense=data.expense_data_list.get(a).item_data_list.get(b).item_cost
                        category.category=data.expense_data_list.get(a).item_data_list.get(b).category
                        month_data.category_wise_expense.add(category)
                    }
                    b++
                }
            }
            else
            {
                month_wise_expense.add(month_data)
                var month_data2 = data_month_wise()
                month_data2.category_wise_expense.clear()
                month_data2.month=data.expense_data_list.get(a).month
                month_data2.year=data.expense_data_list.get(a).year
                month_data = month_data2
                continue
            }
            if(a+1==data.expense_data_list.size)
            {   month_wise_expense.add(month_data)}
            a++
        }
        month_wise_expense.removeAt(0)//because the data at index 0 is dummy.

        var yearSpinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_color_layout2,
            year_list
        )
        yearSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        year_spinner.adapter=yearSpinnerAdapter
        year_spinner.onItemSelectedListener=this

        //first look settings initializer
        category_wise_spending_button.setOnClickListener{
            monthly_spending_button.isEnabled=true
            category_wise_spending_button.isEnabled=false
            year_spinner.isVisible=true
            month_spinner.isVisible=true
            pieChart.isVisible=true
            barChart.isVisible=false
            mode_changing_data.year=year
            mode_changing_data.month=month
            mode_changing_data.index=get_month_index(month,year)
            mode_changing_data.category_mode=true
            recyclerViewAdapter.notifyDataSetChanged()
        }
        category_wise_spending_button.isEnabled=false
        barChart.isVisible=false
        monthly_spending_button.setOnClickListener{
            monthly_spending_button.isEnabled=false
            category_wise_spending_button.isEnabled=true
            year_spinner.isVisible=false
            month_spinner.isVisible=false
            pieChart.isVisible=false
            barChart.isVisible=true
            mode_changing_data.year=year
            mode_changing_data.month=month
            mode_changing_data.index=-1
            mode_changing_data.category_mode=false
            recyclerViewAdapter.notifyDataSetChanged()
        }
        month_spinner.setSelection(month)
        a=0
        while(a<year_list.size-1)
        {
            if(year_list.get(a)==year)
            {   break}
            a++
        }
        year_spinner.setSelection(a)

        //pie data settings. Donot delete the comments here.
        pieChart.elevation=20.0f
        pieChart.isRotationEnabled=true
        pieChart.setUsePercentValues(true)
        var map = listener.get_color_id()
        //pieChart.setCenterTextColor(map["MediumColor"]!!)
        pieChart.holeRadius=0f//35.0f
        pieChart.setCenterTextSize(12.0f)
        //pieChart.setHoleColor(map["backgroundColor"]!!)
        pieChart.setDrawEntryLabels(false)
        pieChart.setEntryLabelTextSize(20.0f)
        pieChart.setEntryLabelColor(map["DeepColor"]!!)
        pieChart.description.textSize=15.0f
        pieChart.description.text="Spending in % per category"
        pieChart.description.textColor=map["MediumColor"]!!
        pieChart.legend.textColor=map["MediumColor"]!!
        pieChart.legend.textSize=15.0f
        pieChart.transparentCircleRadius=30.0f
        addPieChartData(month+1,year,map)

        //recycler_view setup
        var linear_layout = LinearLayoutManager(view.context)
        mode_changing_data.year=year
        mode_changing_data.month=month
        mode_changing_data.category_mode=true
        recyclerViewAdapter = reportRecyclerViewAdapter(map,month_wise_expense,mode_changing_data)
        recycler_view.adapter = recyclerViewAdapter
        recycler_view.layoutManager=linear_layout

        return view
    }

    private fun get_month_index(month: Int,year:Int):Int
    {
        var a=0
        while(a<month_wise_expense.size)
        {
            if(month_wise_expense.get(a).month==month && month_wise_expense.get(a).year==year)
            {   break}
            a++
        }
        return a
    }

    private fun addPieChartData(month: Int, year: Int,map: HashMap<String, Int>)
    {
        pieChart.animateXY(1400, 1400)
        var yEntrys = ArrayList<PieEntry>()
        var a=0
        while(a<month_wise_expense.size)
        {
            if(month_wise_expense.get(a).month==month && month_wise_expense.get(a).year==year)
            {
                //pieChart.centerText="Total spending in month of "+month_list.get(month_wise_expense.get(a).month-1)+": Rs "+month_wise_expense.get(a).total_month_expense
                var b=0
                while(b<month_wise_expense.get(a).category_wise_expense.size)
                {
                    yEntrys.add(PieEntry(month_wise_expense.get(a).category_wise_expense.get(b).total_category_wise_expense,month_wise_expense.get(a).category_wise_expense.get(b).category))
                    b++
                }
                break
            }
            a++
        }
        mode_changing_data.index=a
        month_index=a

        val pieDataSet = PieDataSet(yEntrys,"" )
        pieDataSet.sliceSpace = 2.0f
        pieDataSet.valueTextColor = map["DeepColor"]!!
        pieDataSet.valueTextSize = 20.0f
        pieDataSet.valueFormatter=PercentFormatter(pieChart)
        val colors = ArrayList<Int>()
        val random = Random()
        for (colour in 0..6) {
            colors.add(Integer.valueOf(Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256))))
        }
        val pieData = PieData(pieDataSet)
        //pieDataSet.setDrawValues(false)
        pieDataSet.valueLinePart1OffsetPercentage=90.0f
        pieDataSet.valueLinePart1Length=1f
        pieDataSet.valueLinePart2Length=1f
        pieDataSet.valueLineColor=map["MediumColor"]!!
        pieDataSet.valueLineWidth=3.0f
        pieDataSet.xValuePosition=PieDataSet.ValuePosition.OUTSIDE_SLICE
        pieDataSet.yValuePosition=PieDataSet.ValuePosition.OUTSIDE_SLICE
        //pieDataSet.setValueTextColors(colors)
        pieDataSet.colors=colors
        pieChart.data = pieData
        pieChart.invalidate()
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, v: View?, index: Int, p3: Long) {
        var map = listener.get_color_id()
        if(index>0)
        {   (adapterView!!.getChildAt(0) as TextView).setTextColor(map["MediumColor"]!!)}
        else
        {   (adapterView!!.getChildAt(0) as TextView).setTextColor(map["DeepColor"]!!)}
        if(v==month_spinner.selectedView)
        {   month=index}
        else if(v==year_spinner.selectedView)
        {   year=year_list.get(index)}
        mode_changing_data.year=year
        mode_changing_data.month=month
        addPieChartData(month+1,year,map)
        if(::recyclerViewAdapter.isInitialized)
        {   recyclerViewAdapter.notifyDataSetChanged()}
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}