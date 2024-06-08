package com.example.chucksgourmet

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NestedMenuItemAdapter(private val menuItems: List<MenuItemDC>,private val menuItemQuantity:List<Int>) : RecyclerView.Adapter<NestedMenuItemAdapter.NestedItemViewHolder>() {
    class NestedItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_cart_template, parent, false)
        return NestedItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: NestedItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.itemName.text = "- "+menuItem.itemName
        holder.itemQuantity.text = menuItemQuantity[position].toString()
        holder.itemPrice.text = String.format(
            "%.2f",
            menuItem.itemPrice - (menuItem.itemPrice * menuItem.itemPromotion)
        )
    }

    override fun getItemCount(): Int = menuItems.size
}
