package com.yuvraj.expensemonitor.kotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.yuvraj.expensemonitor.R

class CategoryFragment : Fragment() {

    lateinit var fragment_manager: FragmentManager
    private fun CategoryFragment(fragmentManager: FragmentManager)
    {   fragment_manager = fragmentManager}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false)
    }
}