package com.example.chucksgourmet

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DialogBoxMenuItemRVAdapter(
    private val activity: AppCompatActivity,
    private val addOnList: ArrayList<MenuItemDC>,
    private val price: Double 
) : RecyclerView.Adapter<DialogBoxMenuItemRVAdapter.DialogBoxMenuItemViewHolder>() {

    private var selectedSidesCount = 0
    private var selectedDrinksCount = 0
    private var maxSidesAllowed = 0
    private var maxDrinksAllowed = 0
    private val selectedItems = mutableListOf<MenuItemDC>()
    private var itemQuantities = mutableMapOf<Int, Int>()

    init {
        // Initialize the max sides and drinks allowed based on the price
        when {
            price >= 35 -> {
                maxSidesAllowed = 2
                maxDrinksAllowed = 2
            }
            price >= 25 -> {
                maxSidesAllowed = 1
                maxDrinksAllowed = 2
            }
            price >= 15 -> {
                maxSidesAllowed = 1
                maxDrinksAllowed = 1
            }
        }
    }

    class DialogBoxMenuItemViewHolder(menuItemView: View) : RecyclerView.ViewHolder(menuItemView) {
        val addOnImg: ImageView = itemView.findViewById(R.id.imgDialogBoxAddOn)
        val addOnName: TextView = itemView.findViewById(R.id.tvDialogBoxAddOnName)
        val addOnPrice: TextView = itemView.findViewById(R.id.tvDialogBoxAddOnPrice)
        val addOnQuantity: TextView = itemView.findViewById(R.id.tvAddOnQuantityValue)
        val btnMinusQuantity: Button = itemView.findViewById(R.id.btnAddOnMinus)
        val btnPlusQuantity: Button = itemView.findViewById(R.id.btnAddOnPlus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogBoxMenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_menu_dialog_box_template, parent, false)
        return DialogBoxMenuItemViewHolder(view)
    }


    override fun onBindViewHolder(holder: DialogBoxMenuItemViewHolder, position: Int) {
        val addOnItem = addOnList[position]

        Glide.with(activity)
            .load(addOnItem.imageURL)
            .placeholder(R.drawable.cooked_chicken)
            .into(holder.addOnImg)
        holder.addOnName.text = addOnItem.itemName
        holder.addOnPrice.text = "Price: " + String.format("%.2f", addOnItem.itemPrice - (addOnItem.itemPrice * addOnItem.itemPromotion))

        // Get the current quantity from the map or default to 0
        val currentQuantity = itemQuantities[position] ?: 0
        holder.addOnQuantity.text = currentQuantity.toString()

        // Update button states initially
        updateButtonStates(holder, currentQuantity, addOnItem.itemType)

        holder.btnPlusQuantity.setOnClickListener {
            // Ensure the current position is valid
            if (holder.adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            if (canIncrement(addOnItem.itemType)) {
                val newQuantity = currentQuantity + 1
                itemQuantities[position] = newQuantity
                holder.addOnQuantity.text = newQuantity.toString()

                selectedItems.add(addOnItem)
                updateSelectedCount(addOnItem.itemType, 1)

                // Notify the specific item to update its button states
                notifyItemChanged(position)
                updateAllButtonStates()
                Log.d("MYTAG2", "side count: $selectedSidesCount max side count $maxSidesAllowed")
            }
        }

        holder.btnMinusQuantity.setOnClickListener {
            // Ensure the current position is valid
            if (holder.adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            if (currentQuantity > 0) {
                val newQuantity = currentQuantity - 1
                itemQuantities[position] = newQuantity
                holder.addOnQuantity.text = newQuantity.toString()

                if (newQuantity == 0) {
                    selectedItems.remove(addOnItem)
                }

                updateSelectedCount(addOnItem.itemType, -1)

                // Notify the specific item to update its button states
                notifyItemChanged(position)
                updateAllButtonStates()
                Log.d("MYTAG2", "side count: $selectedSidesCount max side count $maxSidesAllowed")
            }
        }
    }

    private fun canIncrement(itemType: String): Boolean {
        return if (itemType == "sides") {
            selectedSidesCount < maxSidesAllowed
        } else {
            selectedDrinksCount < maxDrinksAllowed
        }
    }

    private fun updateSelectedCount(itemType: String, delta: Int) {
        if (itemType == "sides") {
            selectedSidesCount += delta
        } else if (itemType == "drinks") {
            selectedDrinksCount += delta
        }
    }

    private fun updateButtonStates(holder: DialogBoxMenuItemViewHolder, currentQuantity: Int, itemType: String) {
        holder.btnPlusQuantity.isEnabled = canIncrement(itemType)
        holder.btnPlusQuantity.alpha = if (holder.btnPlusQuantity.isEnabled) 1.0f else 0.5f

        holder.btnMinusQuantity.isEnabled = currentQuantity > 0
        holder.btnMinusQuantity.alpha = if (currentQuantity > 0) 1.0f else 0.5f
    }

    private fun updateAllButtonStates() {
        for (i in addOnList.indices) {
            val currentQuantity = itemQuantities[i] ?: 0
            val itemType = addOnList[i].itemType
            notifyItemChanged(i)
        }
    }

    override fun getItemCount(): Int {
        return addOnList.size
    }

    // Public function to get the list of selected items
    fun getSelectedItems(): List<MenuItemDC> {
        return selectedItems
    }
}
