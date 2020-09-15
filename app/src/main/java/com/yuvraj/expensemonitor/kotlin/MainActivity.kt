package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.Window
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


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer_layout : DrawerLayout
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        var action_bar_toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.open,R.string.close)
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

        //temporary stuff
        var map= get_color_id()
        var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
        map.clear()
        fragmentManager=supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.container_fragment,ExpensesFragment(),"expense_fragment").commit()
        supportActionBar?.title = HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.expense_list_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun get_color_id(): HashMap<String, Int>
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        var map= get_color_id()
        var medium_color = String.format("#%06X", 0xFFFFFF and map["MediumColor"]!!)
        map.clear()

        if(item.itemId==R.id.expense_list_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container_fragment,ExpensesFragment(),"expense_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.expense_list_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.category_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container_fragment,CategoryFragment(),"category_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml("<font color="+medium_color+">" +resources.getString(R.string.category_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.report_item)
        {
            //fragmentManager=supportFragmentManager
            //fragmentManager.beginTransaction().replace(R.id.container_fragment,ExpensesFragment(),"expense_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.report_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.backup_item)
        {
            fragmentManager=supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container_fragment,SyncFragment(),"sync_fragment").commit()
            supportActionBar?.title = HtmlCompat.fromHtml("<font color="+medium_color+">" + resources.getString(R.string.sync_item) + "</font>",HtmlCompat.FROM_HTML_MODE_LEGACY)
            drawer_layout.closeDrawers()
        }
        else if(item.itemId==R.id.about_item)
        {
            drawer_layout.closeDrawers()
        }
        return true
    }

}