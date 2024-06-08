package com.example.chucksgourmet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> get() = _totalPrice

    init {
        _cartItems.value = CartManager.getItems()
        updateTotalPrice()
    }

    fun addItem(menuItem: MenuItemDC?, menuSet: MenuSetDC?, quantity: Int) {
        CartManager.addItem(menuItem, menuSet, quantity)
        _cartItems.value = CartManager.getItems()
        updateTotalPrice()
    }

    fun removeItem(menuItem: MenuItemDC?, menuSet: MenuSetDC?) {
        CartManager.removeItem(menuItem, menuSet)
        _cartItems.value = CartManager.getItems()
        updateTotalPrice()
        Log.d("MYTAG", _cartItems.value!!.size.toString())
    }

    private fun updateTotalPrice() {
        _totalPrice.value = CartManager.getTotalPrice()
    }
}


