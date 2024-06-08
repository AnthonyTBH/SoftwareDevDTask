package com.example.chucksgourmet

import android.util.Log

object CartManager {
    private var cartItems = mutableListOf<CartItem>()

    fun addItem(menuItem: MenuItemDC? = null, menuSet: MenuSetDC? = null, quantity: Int) {
        // Ensure either menuItem or menuSet is provided, not both
        require(!(menuItem == null && menuSet == null) && !(menuItem != null && menuSet != null)) {
            "Either menuItem or menuSet must be provided, but not both."
        }

        val existingCartItem = cartItems.find {
            (menuItem != null && it.menuItem == menuItem) || (menuSet != null && it.menuSet == menuSet)
        }

        if (existingCartItem != null) {
            Log.d("MYTAG", "Duplicate items found")
            existingCartItem.quantities[menuItem ?: menuSet!!] = existingCartItem.quantities.getOrDefault(menuItem ?: menuSet!!, 0) + quantity
        } else {
            val newCartItem = CartItem(menuItem = menuItem, menuSet = menuSet)
            newCartItem.quantities[menuItem ?: menuSet!!] = quantity
            cartItems.add(newCartItem)

        }
    }

    fun removeItem(menuItem: MenuItemDC? = null, menuSet: MenuSetDC? = null) {
        // Ensure either menuItem or menuSet is provided, not both
        require(!(menuItem == null && menuSet == null) && !(menuItem != null && menuSet != null)) {
            "Either menuItem or menuSet must be provided, but not both."
        }

        val cartItem = cartItems.find {
            (menuItem != null && it.menuItem == menuItem) || (menuSet != null && it.menuSet == menuSet)
        }

        if (cartItem != null) {
            val key = menuItem ?: menuSet!!
            val newQuantity = cartItem.quantities[key]?.minus(1) ?: 0
            if (newQuantity <= 0) {
                cartItem.quantities.remove(key)
                cartItems.remove(cartItem)
            } else {
                cartItem.quantities[key] = newQuantity
            }
        }
    }
    fun clearCart(){
        cartItems=mutableListOf()
    }

    fun getItems(): List<CartItem> = cartItems

    fun getTotalPrice(): Double {
        return cartItems.sumOf { cartItem ->
            cartItem.quantities.entries.sumByDouble { (item, quantity) ->
                when (item) {
                    is MenuItemDC -> {
                        val itemPrice = item.itemPrice
                        val promotion = item.itemPromotion
                        (itemPrice * (1 - promotion)) * quantity
                    }
                    is MenuSetDC -> {
                        val setPrice = item.setPrice
                        (setPrice * 0.8) * quantity
                    }
                    else -> 0.0
                }
            }
        }
    }


}
