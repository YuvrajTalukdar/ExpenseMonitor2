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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yuvraj.expensemonitor.R
import com.yuvraj.expensemonitor.java.data_handler
import com.yuvraj.expensemonitor.java.database_handler
import kotlinx.android.synthetic.main.fragment_report.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ReportFragment : Fragment(), AdapterView.OnItemSelectedListener{

    var month_list = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    lateinit var data: data_handler
    lateinit var db: database_handler
    lateinit var lineChart: LineChart
    lateinit var pieChart: PieChart
    internal lateinit var listener: reportFragmentListener
    var year_list = ArrayList<Int>()
    var c=Calendar.getInstance()
    var month=c.get(Calendar.MONTH)+1
    var year=c.get(Calendar.YEAR)
    lateinit var recycler_view: RecyclerView
    lateinit var recyclerViewAdapter: reportRecyclerViewAdapter
    lateinit var statusTextView: TextView
    lateinit var map:HashMap<String,Int>

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
        statusTextView = view.findViewById(R.id.report_recycler_view_status)

        lineChart = view.findViewById(R.id.lineChart)
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
            if(month_data.month == data.expense_data_list.get(a).month && month_data.year == data.expense_data_list.get(
                    a
                ).year)
            {
                var b=0
                while(b<data.expense_data_list.get(a).item_data_list.size)
                {
                    month_data.total_month_expense+=data.expense_data_list.get(a).item_data_list.get(
                        b
                    ).item_cost
                    var c=0
                    var found=false
                    while(c<month_data.category_wise_expense.size)
                    {
                        if(month_data.category_wise_expense.get(c).category.equals(
                                data.expense_data_list.get(
                                    a
                                ).item_data_list.get(b).category
                            ))
                        {
                            month_data.category_wise_expense.get(c).total_category_wise_expense+=data.expense_data_list.get(
                                a
                            ).item_data_list.get(b).item_cost
                            found=true
                            break
                        }
                        c++
                    }
                    if(!found)
                    {
                        var category = data_month_wise.data_category_wise()
                        category.total_category_wise_expense=data.expense_data_list.get(a).item_data_list.get(
                            b
                        ).item_cost
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
            lineChart.isVisible=false
            mode_changing_data.year=year
            mode_changing_data.month=month
            mode_changing_data.index=get_month_index(month, year)
            if(mode_changing_data.index>=month_wise_expense.size)
            {   statusTextView.visibility=View.VISIBLE}
            mode_changing_data.category_mode=true
            recyclerViewAdapter.notifyDataSetChanged()
        }
        category_wise_spending_button.isEnabled=false
        lineChart.isVisible=false
        monthly_spending_button.setOnClickListener{
            monthly_spending_button.isEnabled=false
            category_wise_spending_button.isEnabled=true
            year_spinner.isVisible=false
            month_spinner.isVisible=false
            pieChart.isVisible=false
            lineChart.isVisible=true
            statusTextView.visibility=View.GONE
            mode_changing_data.year=year
            mode_changing_data.month=month
            mode_changing_data.index=-1
            mode_changing_data.category_mode=false
            recyclerViewAdapter.notifyDataSetChanged()
            addLineChartData()
        }
        month_spinner.setSelection(month - 1)
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
        map = listener.get_color_id()
        //pieChart.setCenterTextColor(map["MediumColor"]!!)
        pieChart.holeRadius=0f//35.0f
        pieChart.setCenterTextSize(12.0f)
        //pieChart.setHoleColor(map["backgroundColor"]!!)
        pieChart.setDrawEntryLabels(false)
        pieChart.setEntryLabelTextSize(20.0f)
        pieChart.setEntryLabelColor(map["DeepColor"]!!)
        pieChart.description.textSize=15.0f
        pieChart.description.text="Spending in % per category"
        pieChart.description.setPosition(1000f,700f)
        pieChart.description.textColor=map["MediumColor"]!!
        pieChart.legend.textColor=map["MediumColor"]!!
        pieChart.legend.textSize=15.0f
        pieChart.transparentCircleRadius=30.0f
        pieChart.setExtraOffsets(0f, 0f, 50f, 0f)
        var legend = pieChart.legend
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        addPieChartData(month, year)

        //recycler_view setup
        var linear_layout = LinearLayoutManager(view.context)
        mode_changing_data.year=year
        mode_changing_data.month=month
        mode_changing_data.category_mode=true
        recyclerViewAdapter = reportRecyclerViewAdapter(map, month_wise_expense, mode_changing_data)
        recycler_view.adapter = recyclerViewAdapter
        recycler_view.layoutManager=linear_layout

        //lineChart


        return view
    }


    private fun addLineChartData()
    {
        var yEntrys = ArrayList<Entry>()
        var a=0
        while(a<month_wise_expense.size)
        {
            yEntrys.add(Entry(a.toFloat(), month_wise_expense.get(a).total_month_expense))
            a++
        }
        val formatter1: ValueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return month_list[month_wise_expense[value.toInt()].month-1] +" "+ month_wise_expense[value.toInt()].year
            }
        }
        lineChart.xAxis.valueFormatter=formatter1
        val formatter2: ValueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "Rs "+super.getFormattedValue(value)
            }
        }
        //lineChart.axisLeft.valueFormatter=formatter2
        lineChart.description.text="Amount spend per month"
        lineChart.description.textColor=map["MediumColor"]!!
        lineChart.description.textSize=15.0f
        //lineChart.xAxis.granularity=1f
        lineChart.animateXY(1400,1400)
        lineChart.xAxis.textColor=map["MediumColor"]!!
        lineChart.isDragEnabled=true
        lineChart.setPinchZoom(true)
        lineChart.zoom(-6f,1f,1f,1f)
        lineChart.zoom(6f,1f,1f,1f)
        lineChart.legend.isEnabled=false
        //lineChart.setScaleEnabled(true)
        lineChart.isScaleXEnabled=true
        lineChart.isScaleYEnabled=false
        //lineChart.setBackgroundColor(Color.WHITE)
        lineChart.axisRight.isEnabled=false
        lineChart.axisLeft.textColor=map["MediumColor"]!!
        lineChart.xAxis.gridColor=map["DarkColor"]!!
        var yaxis=lineChart.axisLeft
        yaxis.gridColor=map["DarkColor"]!!
        var lineDateSet = LineDataSet(yEntrys, "")
        lineDateSet.valueTextColor=map["DeepColor"]!!
        lineDateSet.valueTextSize=15f
        lineDateSet.valueFormatter=formatter2
        lineChart.data = LineData(lineDateSet)
    }

    private fun get_month_index(month: Int, year: Int):Int
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

    private fun addPieChartData(month: Int, year: Int)
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
                    yEntrys.add(
                        PieEntry(
                            month_wise_expense.get(a).category_wise_expense.get(b).total_category_wise_expense,
                            month_wise_expense.get(
                                a
                            ).category_wise_expense.get(b).category
                        )
                    )
                    b++
                }
                break
            }
            a++
        }
        mode_changing_data.index=a
        if(a>=month_wise_expense.size)
        {   statusTextView.visibility=View.VISIBLE}
        else
        {   statusTextView.visibility=View.GONE}

        val pieDataSet = PieDataSet(yEntrys, "")
        pieDataSet.sliceSpace = 2.0f
        pieDataSet.valueTextColor = map["DeepColor"]!!
        pieDataSet.valueTextSize = 15.0f
        pieDataSet.valueFormatter=PercentFormatter(pieChart)
        val colors = ArrayList<Int>()
        val random = Random()
        for (colour in 0..6) {
            colors.add(Integer.valueOf(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))))
        }
        val pieData = PieData(pieDataSet)
        //pieDataSet.setDrawValues(false)
        pieDataSet.valueLinePart1OffsetPercentage=90.0f
        pieDataSet.valueLinePart1Length=0.8f
        pieDataSet.valueLinePart2Length=0.5f
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
        {   month=index+1}
        else if(v==year_spinner.selectedView)
        {   year=year_list.get(index)}
        mode_changing_data.year=year
        mode_changing_data.month=month
        addPieChartData(month, year)
        if(::recyclerViewAdapter.isInitialized)
        {   recyclerViewAdapter.notifyDataSetChanged()}
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}