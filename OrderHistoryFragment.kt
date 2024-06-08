package com.example.chucksgourmet

import NotificationDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.oAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryFragment : Fragment() {
    lateinit var btnCancelOrder: Button
    lateinit var btnLogout:ImageButton
    lateinit var btnCart:ImageButton
    lateinit var btnMenu:ImageButton
    lateinit var rvPastOrders:RecyclerView
    lateinit var orderProgressView:ConstraintLayout
    lateinit var orderProgressViewEmpty:ConstraintLayout
    lateinit var imgCurrentProgressArrow:ImageView
    lateinit var tvCurrentProgress:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        initUI(view)
        handleEvents(view)
        return view
    }
    fun showAlertDialogWithActions(context: Context, title: String, message: String, onYesClicked: () -> Unit) {
        // Create an AlertDialog.Builder instance
        val builder = AlertDialog.Builder(context)

        // Set the dialog title and message
        builder.setTitle(title)
            .setMessage(message)

        // Add a "Yes" button
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Execute the action when the "Yes" button is clicked
            onYesClicked.invoke()
            // Dismiss the dialog
            dialog.dismiss()
        }

        // Add a "No" button
        builder.setNegativeButton("No") { dialog, _ ->
            // Dismiss the dialog when the "No" button is clicked
            dialog.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun handleEvents(view: View) {
        btnLogout.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            // Log out the user
            if (auth.currentUser != null) {
                auth.signOut()
            }
            it.findNavController().navigate(R.id.action_orderHistoryFragment_to_loginFragment)
        }
        btnCancelOrder.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val ordersCollectionRef = firestore.collection("orders")
            val pendingOrdersQuery = ordersCollectionRef
                .whereIn("orderStatus", listOf("pending", "preparing", "ready for pickup"))
                .whereEqualTo("user", FirebaseAuth.getInstance().currentUser?.email)

            pendingOrdersQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val orderStatus = document.data?.get("orderStatus") as? String
                        when(orderStatus){
                            "pending"->{
                                showAlertDialogWithActions(requireActivity(), "Cancel Order", "Confirm cancelling this order?") {
                                    // Update the order status to "cancelled"
                                    document.reference.update("orderStatus", "cancelled")
                                        .addOnSuccessListener {
                                            // Successfully updated
                                            Log.d("TAG", "Document ${document.id} updated successfully")
                                        }
                                        .addOnFailureListener { e ->
                                            // Failed to update
                                            Log.e("TAG", "Error updating document ${document.id}", e)
                                        }
                                    orderProgressView.visibility = View.INVISIBLE
                                    orderProgressViewEmpty.visibility = View.VISIBLE
                                }
                                }
                            "preparing"->{
                                NotificationDialog(
                                    requireActivity(),
                                    "Sorry you can't cancel an order at this stage"
                                ).show()
                            }
                            "ready for pickup"->{
                                NotificationDialog(
                                    requireActivity(),
                                    "Sorry you can't cancel an order at this stage"
                                ).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    Log.e("TAG", "Error getting documents: ", e)
                }
        }
        btnMenu.setOnClickListener {
            it.findNavController().navigate(R.id.action_orderHistoryFragment_to_menuFragment)
        }
        btnCart.setOnClickListener {
            it.findNavController().navigate(R.id.action_orderHistoryFragment_to_viewCartFragment)
        }
    }

    private fun initUI(view: View) {
        btnLogout=view.findViewById(R.id.imgBtnLogout)
        btnCancelOrder=view.findViewById(R.id.btnCancel)
        btnMenu=view.findViewById(R.id.imgNavMenu)
        btnCart=view.findViewById(R.id.imgNavCart)
        rvPastOrders=view.findViewById(R.id.rvCustomerPastOrders)
        orderProgressView = view.findViewById(R.id.orderProgressView)
        orderProgressViewEmpty = view.findViewById(R.id.orderProgressViewEmpty)
        imgCurrentProgressArrow = view.findViewById(R.id.imgCurrentProgressArrow)
        tvCurrentProgress = view.findViewById(R.id.tvCurrentProgress)

        // Get the current user's email address from Firebase Auth
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

// Access the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

// Define the collection and the query
        val ordersCollectionRef = firestore.collection("orders")
        val pendingOrdersQuery = ordersCollectionRef
            .whereIn("orderStatus", listOf("pending", "preparing", "ready for pickup"))
            .whereEqualTo("user", currentUserEmail)
// Execute the query
        pendingOrdersQuery.get()
            .addOnSuccessListener { querySnapshot ->
                // Check if there is any pending order
                val hasPendingOrders = !querySnapshot.isEmpty
                Log.d("MYTAG","current status is "+hasPendingOrders.toString())

                if (hasPendingOrders) {
                    // Iterate through each document in the query snapshot
                    for (document in querySnapshot.documents) {
                        val data = document.data
                        if (data != null) {
                            // Retrieve the order status
                            val orderStatus = data["orderStatus"] as? String
                            Log.d("MYTAG","current status is "+orderStatus)
                            // Perform actions based on the order status
                            when (orderStatus) {
                                "pending" -> {
                                    // Show the layout for pending orders
                                    orderProgressView.visibility = View.VISIBLE
                                    orderProgressViewEmpty.visibility = View.INVISIBLE
                                    btnCancelOrder.background.alpha = 255
                                }
                                "preparing", "ready for pickup" -> {
                                    // Show the layout for pending orders
                                    orderProgressView.visibility = View.VISIBLE
                                    orderProgressViewEmpty.visibility = View.INVISIBLE
                                    val params = imgCurrentProgressArrow.layoutParams as ConstraintLayout.LayoutParams
                                    params.topToBottom = if (orderStatus == "preparing") R.id.tvPreparing else R.id.tvReadyForPickup
                                    params.startToStart = if (orderStatus == "preparing") R.id.tvPreparing else R.id.tvReadyForPickup
                                    params.endToEnd = if (orderStatus == "preparing") R.id.tvPreparing else R.id.tvReadyForPickup
                                    imgCurrentProgressArrow.layoutParams = params

                                    val params2 = tvCurrentProgress.layoutParams as ConstraintLayout.LayoutParams
                                    params2.topToBottom = R.id.imgCurrentProgressArrow
                                    params2.startToStart = if (orderStatus == "preparing") R.id.tvPreparing else R.id.tvReadyForPickup
                                    params2.endToEnd = if (orderStatus == "preparing") R.id.tvPreparing else R.id.tvReadyForPickup
                                    btnCancelOrder.background.alpha = 50

                                    tvCurrentProgress.layoutParams = params2
                                }
                                // Add more cases for other statuses as needed
                                else -> {
                                    // Show the layout for pending orders
                                    orderProgressView.visibility = View.INVISIBLE
                                    orderProgressViewEmpty.visibility = View.VISIBLE
                                    btnCancelOrder.background.alpha = 255
                                }
                            }
                        }
                    }
                } else {
                    // Show the layout for no orders yet
                    orderProgressView.visibility = View.INVISIBLE
                    orderProgressViewEmpty.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                exception.printStackTrace()
            }

        fetchOrdersByCustomerEmail { orders ->
            // Update the RecyclerView with the fetched orders
            rvPastOrders.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            val adapter = PastOrderRVAdapter(orders)
            rvPastOrders.adapter = adapter
        }


    }
    fun fetchOrdersByCustomerEmail(callback: (List<Order>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val ordersList = mutableListOf<Order>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        firestore.collection("orders")
            .whereEqualTo("user", FirebaseAuth.getInstance().currentUser?.email)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
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
                            customer = FirebaseAuth.getInstance().currentUser?.email.toString()
                        )

                        ordersList.add(order)
                    }
                }

                val sortedOrdersList = ordersList.sortedByDescending { order ->
                    try {
                        dateFormat.parse("${order.orderDate} ${order.orderTime}")
                    } catch (e: Exception) {
                        Date(0) // Assign a default date far in the past in case of parsing error
                    }
                }
                callback(sortedOrdersList)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback(emptyList())
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

        return MenuSetDC(
            this["setName"] as? String ?: "",
            (this["setPrice"] as? Double) ?: 0.0,
            1,
            itemsList as ArrayList<MenuItemDC>,
            ArrayList()
        )
    }


}      