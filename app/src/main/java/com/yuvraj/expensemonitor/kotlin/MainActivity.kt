package com.yuvraj.expensemonitor.kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.yuvraj.expensemonitor.R


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
                                          add_category_dialog.add_category_listener,
                                          add_expense_dialog.add_expense_listener{

    private lateinit var drawer_layout : DrawerLayout
    private lateinit var fragmentManager: FragmentManager
    private var checkBox_List: ArrayList<CheckBox> = ArrayList()
    private var current_color_scheme=1;
    private lateinit var settings_reader: SharedPreferences;
    private lateinit var settings_editor: SharedPreferences.Editor
    private var current_fragment_code=-1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings_reader = getSharedPreferences("settings",Context.MODE_PRIVATE)
        color_scheme_changer(settings_reader.getInt("color_scheme_code",3),true)
        save_color_scheme_settings(settings_reader.getInt("color_scheme_code",3))
        current_color_scheme=settings_reader.getInt("color_scheme_code",3)

        //setTheme(R.style.DarkGreenTheme_NoActionBar)

        setContentView(R.layout.activity_main)

        //main activity elements
        var toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //for drawer menu item
        var  navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.itemIconTintList = null;//for default colour icons

        //for drawer action bar
        drawer_layout = findViewById(R.id.drawer_layout)
        var action_bar_toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawer_layout.addDrawerListener(action_bar_toggle)
        action_bar_toggle.isDrawerIndicatorEnabled = true
        action_bar_toggle.syncState()

        //for status bar and navigation bar color
        var window: Window = window
        window.statusBarColor = ContextCompat.getColor(this, R.color.DarkGrey)
        window.navigationBarColor = resources.getColor(R.color.Black, null)

        //for drawer header
        var drawer_header_view: View = navigationView.inflateHeaderView(R.layout.drawer_header)
        var drawer_header_textview: TextView = drawer_header_view.findViewById(R.id.drawer_header_textView)
        drawer_header_textview.setOnClickListener{
            //sign in/sign out
        }
        var header_background: ImageView = drawer_header_view.findViewById(R.id.headerBackground)
        Glide.with(this).load(R.drawable.drawer_header_background).into(header_background)

        //theme
        var drawer_item_linear_layout = navigationView.menu.findItem(R.id.theme_menu_item).actionView
        var redScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.red_color_scheme)
        redScheme.setOnClickListener{
            color_scheme_changer(0,false)
        }
        var greenScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.green_color_scheme)
        greenScheme.setOnClickListener{
            color_scheme_changer(1,false)
        }
        var greyScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.grey_color_scheme)
        greyScheme.setOnClickListener{
            color_scheme_changer(2,false)
        }
        var blueScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.blue_color_scheme)
        blueScheme.setOnClickListener{
            color_scheme_changer(3,false)
        }
        var violetScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.violet_color_scheme)
        violetScheme.setOnClickListener{
            color_scheme_changer(4,false)
        }
        var pinkScheme:CheckBox = drawer_item_linear_layout.findViewById(R.id.pink_color_scheme)
        pinkScheme.setOnClickListener{
            color_scheme_changer(5,false)
        }
        checkBox_List.clear()
        checkBox_List.add(redScheme)
        checkBox_List.add(greenScheme)
        checkBox_List.add(greenScheme)
        checkBox_List.add(blueScheme)
        checkBox_List.add(violetScheme)
        checkBox_List.add(pinkScheme)
        //temporary stuff, need to be replaces with proper saving mechanism
        var map= get_color_id()
        var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
        map.clear()
        if(savedInstanceState!=null)
        {
            if(savedInstanceState.getInt("current_fragment_code")==1)
            {
                supportActionBar?.setTitle(HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.expense_list_item)+ "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY))
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.container_fragment, ExpensesFragment(), "expense_fragment").commit()
                current_fragment_code=1
            }
            else if (savedInstanceState.getInt("current_fragment_code") == 2)
            {
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.container_fragment, CategoryFragment(), "category_fragment").commit()
                supportActionBar?.setTitle(HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.category_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY))
                current_fragment_code=2;
            }
            else if (savedInstanceState.getInt("current_fragment_code") == 3)
            {
                /*fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container_fragment, new Sync_Fragment(), "syncFragment").commit();
                getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color="+medium_color+">" + "Cloud Sync & Local Backup" + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY));
                current_fragment_code=3;*/
            }
            else if(savedInstanceState.getInt("current_fragment_code") == 4)
            {
                fragmentManager = supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.container_fragment, SyncFragment(), "sync_fragment").commit()
                supportActionBar?.title=HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.sync_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
                current_fragment_code=4;
            }
        }
        else
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container_fragment, ExpensesFragment(), "expense_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml("<font color=" + medium_color + ">" + resources.getString(R.string.expense_list_item) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            current_fragment_code=1
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        state.putInt("current_fragment_code",current_fragment_code)
        super.onSaveInstanceState(state)
    }

    //Theme functions
    private fun save_color_scheme_settings(color_scheme: Int)
    {
        settings_reader = getSharedPreferences("settings", Context.MODE_PRIVATE);
        settings_editor = getSharedPreferences("settings",Context.MODE_PRIVATE).edit();
        settings_editor.putInt("color_scheme_code", color_scheme);
        settings_editor.apply();///commit()
    }
    private fun color_scheme_changer(color_scheme_code: Int,first_start: Boolean)
    {
        if(!first_start && current_color_scheme!=color_scheme_code)
        {
            save_color_scheme_settings(color_scheme_code)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else if(!first_start && current_color_scheme==color_scheme_code)
        {   checkBox_List.get(color_scheme_code).setChecked(true)}
        if(color_scheme_code==0 && first_start)
        {   setTheme(R.style.DarkRedTheme_NoActionBar)}
        else if(color_scheme_code==1 && first_start)
        {    setTheme(R.style.DarkGreenTheme_NoActionBar)}
        else if(color_scheme_code==2 && first_start)
        {   setTheme(R.style.DarkGreyTheme_NoActionBar)}
        else if(color_scheme_code==3 && first_start)
        {   setTheme(R.style.DarkBlueTheme_NoActionBar)}
        else if(color_scheme_code==4 && first_start)
        {   setTheme(R.style.DarkVioletTheme_NoActionBar)}
        else if(color_scheme_code==5 && first_start)
        {   setTheme(R.style.DarkPinkTheme_NoActionBar)}
    }

    //Expense Fragment functions
    override fun ExpenseDialogOnOKClicked(dialog_code: Int,item_name: String,item_cost: Float,item_category: String,day: Int,month: Int,year: Int)
    {
        var expenseFragment: ExpensesFragment = supportFragmentManager.findFragmentByTag("expense_fragment") as ExpensesFragment
        if(expenseFragment!=null)
        {
            if(dialog_code==0 || dialog_code==1)
            {   expenseFragment.addExpenseData(item_name,item_cost,item_category,day,month,year)}
        }
    }

    //Category Fragment functions
    override fun CategoryDialogOnOKClicked(dialog_code: Int, category_name: String) {
        if(dialog_code==0)//adding new category
        {
            var category_fragment: CategoryFragment = supportFragmentManager.findFragmentByTag("category_fragment") as CategoryFragment
            if(category_fragment!=null)
            {   category_fragment.add_new_category(category_name)}

        }
        else if(dialog_code==1)//renaming category name
        {
            var category_fragment: CategoryFragment = supportFragmentManager.findFragmentByTag("category_fragment") as CategoryFragment
            if(category_fragment!=null)
            {   category_fragment.rename_category(category_name)}
        }
    }

    //Other random functions
    override fun get_color_id(): HashMap<String, Int>
    {
        var map= HashMap<String, Int>()
        var  typedValue1 =  TypedValue()
        theme.resolveAttribute(R.attr.DeepColor, typedValue1, true)
        map.put("DeepColor", ContextCompat.getColor(this, typedValue1.resourceId))

        var typedValue2 =  TypedValue();
        theme.resolveAttribute(R.attr.MediumColor, typedValue2, true)
        map.put("MediumColor", ContextCompat.getColor(this, typedValue2.resourceId))

        return map
    }

    //Drawer menu functions
    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var map= get_color_id()
        var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
        map.clear()

        if(item.itemId==R.id.expense_list_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(
                R.id.container_fragment,
                ExpensesFragment(),
                "expense_fragment"
            ).commit()
            supportActionBar?.title = HtmlCompat.fromHtml(
                "<font color=" + medium_color + ">" + resources.getString(
                    R.string.expense_list_item
                ) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            current_fragment_code=1
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.category_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(
                R.id.container_fragment,
                CategoryFragment(),
                "category_fragment"
            ).commit()
            supportActionBar?.title = HtmlCompat.fromHtml(
                "<font color=" + medium_color + ">" + resources.getString(
                    R.string.category_item
                ) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            current_fragment_code=2
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.report_item)
        {
            //fragmentManager=supportFragmentManager
            //fragmentManager.beginTransaction().replace(R.id.container_fragment,ExpensesFragment(),"expense_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml(
                "<font color=" + medium_color + ">" + resources.getString(
                    R.string.report_item
                ) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            current_fragment_code=3
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.backup_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(
                R.id.container_fragment,
                SyncFragment(),
                "sync_fragment"
            ).commit()
            supportActionBar?.title = HtmlCompat.fromHtml(
                "<font color=" + medium_color + ">" + resources.getString(
                    R.string.sync_item
                ) + "</font>", HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            current_fragment_code=4
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.about_item)
        {
            drawer_layout.closeDrawers()
        }
        return true
    }

}