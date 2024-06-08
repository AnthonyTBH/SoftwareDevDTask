package com.example.chucksgourmet

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class CartRVAdapter(private var items: List<CartItem>, private val cartViewModel: CartViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_SET = 2
    }
    interface OnCartItemClickListener {
        fun onMenuItemClick(menuItem: MenuItemDC)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER
        else {
            val adjustedPosition = position - 1
            val cartItem = items[adjustedPosition]
            if (cartItem.menuSet != null) VIEW_TYPE_SET
            else VIEW_TYPE_ITEM
        }
    }

    // Function to set the items
    fun setItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_cart_template, parent, false)
                ItemViewHolder(view)
            }
            VIEW_TYPE_SET -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_cart_set_template, parent, false)
                SetViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val adjustedPosition = position - 1 // Adjust position for header
            val cartItem = items[adjustedPosition]
            if (cartItem.menuItem != null) {
                holder.itemName.text = cartItem.menuItem.itemName
                holder.itemQuantity.text = "${cartItem.quantities[cartItem.menuItem] ?: 0}" // Get the quantity for the menuItem
                val itemPrice = cartItem.menuItem.itemPrice
                val itemPromotion = cartItem.menuItem.itemPromotion
                val discountedPrice = itemPrice - (itemPrice * itemPromotion)
                holder.itemPrice.text = String.format("%.2f", discountedPrice)
                holder.itemView.setOnClickListener {
                    Log.d("MYTAG","remove")
                    cartViewModel.removeItem(cartItem.menuItem, null)
                }
            }
        } else if (holder is SetViewHolder) {
            val adjustedPosition = position - 1 // Adjust position for header
            val cartItem = items[adjustedPosition]
            if (cartItem.menuSet != null) {
                holder.setName.text = cartItem.menuSet.setName
                holder.setQuantity.text = "${cartItem.quantities[cartItem.menuSet] ?: 0}"
                holder.setPrice.text = ""
                val nestedAdapter = NestedMenuItemAdapter(cartItem.menuSet.menuItems, cartItem.menuSet.menuItemQuantities)
                holder.nestedRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.nestedRecyclerView.adapter = nestedAdapter

                holder.itemView.setOnClickListener {
                    cartViewModel.removeItem(null, cartItem.menuSet)
                }
            }
        }

    }


    override fun getItemCount(): Int {
        return items.size + 1 // Include header
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
    }
    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val setName: TextView = view.findViewById(R.id.setName)
        val setQuantity:TextView = view.findViewById(R.id.setQuantity)
        val setPrice:TextView = view.findViewById(R.id.setPrice)
        val nestedRecyclerView: RecyclerView = view.findViewById(R.id.nestedRecyclerView)
    }

}

