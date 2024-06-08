package com.example.chucksgourmet

data class CartItem(
    val menuItem: MenuItemDC? = null,
    val menuSet: MenuSetDC? = null,
    val quantities: MutableMap<Any, Int> = mutableMapOf()
)

