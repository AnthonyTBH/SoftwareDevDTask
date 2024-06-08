package com.example.chucksgourmet

import android.graphics.Typeface
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrderFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: OrdersPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var imgButtonAdminDashboard:ImageButton
    private lateinit var imgButtonAdminMenu:ImageButton
    private lateinit var imgButtonLogout:ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_order, container, false)
        initUI(view)
        fetchOrders()
        handleEvents()
        return view
    }

    private fun handleEvents() {
         imgButtonAdminDashboard.setOnClickListener{
             it.findNavController().navigate(R.id.action_adminOrderFragment_to_adminDashboardFragment)
         }
        imgButtonAdminMenu.setOnClickListener{
            it.findNavController().navigate(R.id.action_adminOrderFragment_to_adminMenuFragment)
        }
        imgButtonLogout.setOnClickListener{
            it.findNavController().navigate(R.id.action_adminOrderFragment_to_loginFragment)
        }
    }

    private fun initUI(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.adminOrderTab)
        imgButtonAdminDashboard = view.findViewById(R.id.imgNavAnalytics)
        imgButtonAdminMenu = view.findViewById(R.id.imgNavMenu)
        imgButtonLogout=view.findViewById(R.id.imgBtnLogout)
    }

    private fun setupTabLayout() {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending"
                1 -> "Preparing"
                2 -> "Ready for Pickup"
                3 -> "Completed"
                4 -> "Cancelled"
                else -> "Tab $position"
            }
        }.attach()
    }

    private fun setupViewPager(
        pendingOrders: List<Order>,
        preparingOrders: List<Order>,
        readyForPickupOrders: List<Order>,
        completedOrders: List<Order>,
        cancelledOrders: List<Order>
    ) {
        val pendingFragment = PendingOrdersFragment().apply { setOrders(pendingOrders) }
        val preparingFragment = PendingOrdersFragment().apply { setOrders(preparingOrders) }
        val readyForPickupFragment = PendingOrdersFragment().apply { setOrders(readyForPickupOrders) }
        val completedFragment = PendingOrdersFragment().apply { setOrders(completedOrders) }
        val cancelledFragment = PendingOrdersFragment().apply { setOrders(cancelledOrders) }

        pagerAdapter = OrdersPagerAdapter(
            requireActivity(),
            listOf(pendingFragment, preparingFragment, readyForPickupFragment, completedFragment, cancelledFragment)
        )
        viewPager.adapter = pagerAdapter
    }
    fun fetchOrders() {
        val firestore = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        firestore.collection("orders")
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w("FirestoreListener", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    val pendingOrders = mutableListOf<Order>()
                    val preparingOrders = mutableListOf<Order>()
                    val readyForPickupOrders = mutableListOf<Order>()
                    val completedOrders = mutableListOf<Order>()
                    val cancelledOrders = mutableListOf<Order>()

                    for (document in querySnapshot.documents) {
                        val data = document.data
                        if (data != null) {
                            val orderedItems = data["orderedItems"] as? ArrayList<Map<String, Any>> ?: ArrayList()
                            val orderedSets = data["orderedSets"] as? ArrayList<Map<String, Any>> ?: ArrayList()

                            val orderedItemsList = orderedItems.mapNotNull { item ->
                                item.toMenuItemDC()
                            }.toMutableList() as ArrayList<MenuItemDC>

                            val orderedSetsList = orderedSets.mapNotNull { set ->
                                set.toMenuSetDC()
                            }.toMutableList() as ArrayList<MenuSetDC>

                            val orderItemQuantity = data["orderItemQuantity"] as? ArrayList<Int> ?: ArrayList()
                            val orderSetQuantity = data["orderSetQuantity"] as? ArrayList<Int> ?: ArrayList()

                            val orderDate = data["orderDate"] as? String ?: ""
                            val orderTime = data["orderTime"] as? String ?: ""

                            val order = Order(
                                orderedItems = orderedItemsList,
                                orderItemQuantity = orderItemQuantity,
                                orderedSets = orderedSetsList,
                                orderSetQuantity = orderSetQuantity,
                                total = (data["total"] as? Double) ?: 0.0,
                                orderDate = orderDate,
                                orderTime = orderTime,
                                orderStatus = data["orderStatus"] as? String ?: "",
                                location = data["location"] as? String ?: "",
                                address = data["address"] as? String ?: "",
                                customer = data["user"] as? String ?: ""
                            )
                            when (order.orderStatus) {
                                "pending" -> pendingOrders.add(order)
                                "preparing" -> preparingOrders.add(order)
                                "ready for pickup" -> readyForPickupOrders.add(order)
                                "completed" -> completedOrders.add(order)
                                "cancelled" -> cancelledOrders.add(order)
                            }
                        }
                    }

                    val sortedPendingList = pendingOrders.sortedByDescending { order ->
                        try {
                            dateFormat.parse("${order.orderDate} ${order.orderTime}")
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }
                    val sortedPreparingList = preparingOrders.sortedByDescending { order ->
                        try {
                            dateFormat.parse("${order.orderDate} ${order.orderTime}")
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }
                    val sortedReadyPickupList = readyForPickupOrders.sortedByDescending { order ->
                        try {
                            dateFormat.parse("${order.orderDate} ${order.orderTime}")
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }
                    val sortedCompletedList = completedOrders.sortedByDescending { order ->
                        try {
                            dateFormat.parse("${order.orderDate} ${order.orderTime}")
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }
                    val sortedCancelledList = cancelledOrders.sortedByDescending { order ->
                        try {
                            dateFormat.parse("${order.orderDate} ${order.orderTime}")
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }

                    setupViewPager(sortedPendingList, sortedPreparingList, sortedReadyPickupList, sortedCompletedList, sortedCancelledList)
                    setupTabLayout()
                }
            }
    }



    fun Map<String, Any>.toMenuItemDC(): MenuItemDC? {
        return MenuItemDC(
            "",
            this["itemName"] as? String ?: "",
            (this["itemPrice"] as? Double) ?: 0.0,
            "",
            0.0,
        )
    }

    fun Map<String, Any>.toMenuSetDC(): MenuSetDC? {
        val itemsList = (this["menuItems"] as? List<Map<String, Any>>)?.mapNotNull { it.toMenuItemDC() }?.toMutableList() ?: ArrayList()
        val itemListQuantity = this["menuItemQuantity"] as? ArrayList<Int> ?: ArrayList() // Remove the extra closing parenthesis
        return MenuSetDC(
            this["setName"] as? String ?: "",
            (this["setPrice"] as? Double) ?: 0.0,
            1,
            itemsList as ArrayList<MenuItemDC>,
            itemListQuantity 
        )
    }



}
