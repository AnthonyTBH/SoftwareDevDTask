package com.example.chucksgourmet

import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class MenuItemFragment : Fragment() {
    private lateinit var MenuItemRV: RecyclerView
    private var param1: ArrayList<MenuItemDC>? = null
    private var isAdmin:Boolean?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelableArrayList("param1")
            isAdmin = if (it.containsKey("param2")) {
                it.getBoolean("param2")
            } else {
                null
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu_item, container, false)
        initUI(view)
        return view
    }

    private fun initUI(view: View) {
        MenuItemRV = view.findViewById(R.id.rvMenuItem)
        MenuItemRV.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        Log.d("MYTAG","in MenuItemFragment, "+isAdmin.toString())
        val adapter = MenuItemRVAdapter(requireActivity() as AppCompatActivity,param1!!,ViewModelProvider(this).get(CartViewModel::class.java),isAdmin,childFragmentManager,requireContext())
        MenuItemRV.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: ArrayList<MenuItemDC>,isAdmin:Boolean?=null) =
            MenuItemFragment().apply {
                Log.d("MYTAG","in MenuItemFragmentNewInstance, "+isAdmin.toString())
                arguments = Bundle().apply {
                    putParcelableArrayList("param1", param1)
                    if (isAdmin != null) {
                        putBoolean("param2",isAdmin)
                    }
                }
            }
    }
}