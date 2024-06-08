package com.example.chucksgourmet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PendingOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: PastOrderRVAdapter
    private val ordersLiveData = MutableLiveData<List<Order>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pending_orders, container, false)
        recyclerView = view.findViewById(R.id.rvAdminOrders)
        recyclerView.layoutManager = LinearLayoutManager(context)
        orderAdapter = PastOrderRVAdapter(emptyList(),true)
        recyclerView.adapter = orderAdapter

        ordersLiveData.observe(viewLifecycleOwner, { orders ->
            orderAdapter.updateOrders(orders)
        })

        return view
    }

    fun setOrders(orders: List<Order>) {
        ordersLiveData.value = orders
    }
}

