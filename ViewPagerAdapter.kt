package com.example.chucksgourmet

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(appCompatActivity: AppCompatActivity, private val menuItems:ArrayList<ArrayList<MenuItemDC>>,private val isAdmin:Boolean?=null):
    FragmentStateAdapter(appCompatActivity) {
    override fun getItemCount(): Int =menuItems.size

    override fun createFragment(position: Int): Fragment {
        Log.d("MYTAG","in ViewPager, "+isAdmin.toString())

        return MenuItemFragment.newInstance(menuItems[position],isAdmin)
    }
}