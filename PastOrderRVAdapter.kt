package com.example.chucksgourmet

import NotificationDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastOrderRVAdapter(private var orders: List<Order>,private var isAdmin:Boolean?=null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.order_history_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_order_history, parent, false)
                ItemViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val adjustedPosition = position - 1 // Adjust position for header
            val order = orders[adjustedPosition]
            holder.orderTimeStamp.text = "${order.orderDate} ${order.orderTime}"

            holder.orderDetails.text = buildString {
                // Append ordered items with their quantities
                for ((index, menuItem) in order.orderedItems.withIndex()) {
                    append("${menuItem.itemName} x${order.orderItemQuantity[index]}\n")
                }
                // Append a line break if both orderedItems and orderedSets are not empty
                if (order.orderedItems.isNotEmpty() && order.orderedSets.isNotEmpty()) {
                    append("\n")
                }
                // Append ordered sets with their quantities
                for ((index, menuSet) in order.orderedSets.withIndex()) {
                    append("${menuSet.setName} x${order.orderSetQuantity[index]}\n")
                }
            }.trimEnd() // Remove the last newline character

            holder.orderPrice.text = "RM" + String.format("%.2f", order.total)

            if (isAdmin != null) {
                // Show dialog and handle actions when an order is clicked
                holder.itemView.setOnClickListener {

                    showAlertDialogWithActions(
                        context,
                        "Order Details",
                        getOrderDetails(order)+"\n\nDo you want to proceed with this order?",
                        // Lambda function to handle the "Yes" button action
                        onYesClicked = {
                            // Perform Firebase query to update the order status based on the current status
                            // Example:
                            when (order.orderStatus) {
                                "pending" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "preparing",
                                        "Order has proceeded to preparing"
                                    )
                                    MyFirebaseMessagingService.sendNotificationToDevice(context,"Order Update","Hello we are currently preparing your order")
                                }

                                "preparing" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "ready for pickup",
                                        "Order has proceeded to ready for pickup"
                                    )
                                    MyFirebaseMessagingService.sendNotificationToDevice(context,"Order Update","Hello your order is ready to be pickup")

                                }
                                "ready for pickup" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "completed",
                                        "Order has proceeded to completed"
                                    )
                                    MyFirebaseMessagingService.sendNotificationToDevice(context,"Order Update","Hello your order has been completed")
                                }
                                "cancelled"->{
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "pending",
                                        "Order has proceeded to pending"
                                    )
                                    MyFirebaseMessagingService.sendNotificationToDevice(context,"Order Update","Hello, your order has been recreated, sorry for the inconvenience")
                                }
                            }
                        }
                    )
                }
                holder.itemView.setOnLongClickListener {
                    showAlertDialogWithActions(
                        context,
                        "Order Details",
                        if (order.orderStatus != "pending") {
                            "Do you want to backtrack this order?"
                        } else {
                            "Do you want to cancel this order?"
                        },
                        // Lambda function to handle the "Yes" button action
                        onYesClicked = {
                            // Perform Firebase query to update the order status based on the current status
                            // Example:
                            when (order.orderStatus) {
                                "pending" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "cancelled",
                                        "Order has been cancelled"
                                    )
                                    MyFirebaseMessagingService.sendNotificationToDevice(context,"Order Update","Hello unfortunately, your order has been cancelled, sorry for the inconvenience")
                                }

                                "preparing" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "pending",
                                        "Order has backtracked to pending"
                                    )
                                }
                                "ready for pickup" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "preparing",
                                        "Order has backtracked to preparing"
                                    )
                                }
                                "completed" -> {
                                    // Proceed to the next status
                                    updateOrderStatus(
                                        order,
                                        "ready for pickup",
                                        "Order has backtracked to ready for pickup"
                                    )
                                }
                            }
                        }
                    )
                    true
                }
            }
        }
    }
    fun getOrderDetails(order: Order): String {
        val builder = StringBuilder()

        // Append ordered items with their quantities
        builder.append("Ordered Items:\n")
        for (i in order.orderedItems.indices) {
            builder.append("${order.orderItemQuantity[i]} x ${order.orderedItems[i].itemName}\n")
        }

        // Append dashed line for separation
        builder.append("\n---------------------------------------------------------------\n\n")

        // Append ordered sets with their quantities and menu items
        builder.append("Ordered Sets:\n")
        for (setIndex in order.orderedSets.indices) {
            val set = order.orderedSets[setIndex]
            builder.append("Set Name: ${set.setName}\n")
            builder.append("Set Price: RM${String.format("%.2f", set.setPrice)}\n")
            builder.append("Quantity: ${set.quantity}\n")

            // Append menu items in this set with their quantities
            builder.append("Menu Items:\n")
            for (itemIndex in set.menuItems.indices) {
                val menuItem = set.menuItems[itemIndex]
                val quantity = set.menuItemQuantities[itemIndex]
                builder.append("$quantity x ${menuItem.itemName}\n")
            }
            builder.append("\n")
        }

        // Append dashed line for separation
        builder.append("\n---------------------------------------------------------------\n\n")

        // Append total, order date, time, status, location, address, and customer
        builder.append("Total: RM${String.format("%.2f", order.total)}\n")
        builder.append("Order Date: ${order.orderDate}\n")
        builder.append("Order Time: ${order.orderTime}\n")
        builder.append("Order Status: ${order.orderStatus}\n")
        builder.append("Location: ${order.location}\n")
        builder.append("Address: ${order.address}\n")
        builder.append("Customer: ${order.customer}\n")

        return builder.toString()
    }

    fun updateOrderStatus(order: Order, newStatus: String,message: String) {
        // Perform Firebase query to find the order based on user, order date, and order time
        val firestore = FirebaseFirestore.getInstance()
        val ordersCollectionRef = firestore.collection("orders")

        // Construct the query
        val query = ordersCollectionRef
            .whereEqualTo("user", order.customer)
            .whereEqualTo("orderDate", order.orderDate)
            .whereEqualTo("orderTime", order.orderTime)
        Log.d("MYTAG","user here "+order.customer)
        Log.d("MYTAG","date here "+order.orderDate)
        Log.d("MYTAG","time here "+order.orderTime)

        // Execute the query
        query.get()
            .addOnSuccessListener { querySnapshot ->
                // Iterate through each document in the query result
                for (document in querySnapshot.documents) {
                    // Get the order document ID
                    val orderId = document.id

                    // Update the order status
                    val orderRef = ordersCollectionRef.document(orderId)
                    orderRef.update("orderStatus", newStatus)
                        .addOnSuccessListener {
                            NotificationDialog(
                                context,
                                message
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                // Show a message or perform any other actions if needed
                Log.e("MYTAG", "Error updating order status", e)
            }
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
    fun updateOrders(orders: List<Order>) {
        this.orders = orders
        notifyDataSetChanged()
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
    override fun getItemCount(): Int {
        return if (orders.isEmpty()) {
            1 // Only header view when orders list is empty
        } else {
            orders.size + 1 // Include header
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderTimeStamp: TextView = view.findViewById(R.id.orderTimeStamp)
        val orderDetails: TextView = view.findViewById(R.id.orderDetails)
        val orderPrice: TextView = view.findViewById(R.id.orderPrice)
    }
}
