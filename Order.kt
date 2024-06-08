package com.example.chucksgourmet

data class Order(var orderedItems:ArrayList<MenuItemDC>,var orderItemQuantity:ArrayList<Int>, var orderedSets:ArrayList<MenuSetDC>,var orderSetQuantity:ArrayList<Int>, var total:Double, var orderDate:String, var orderTime:String, var orderStatus:String, var location:String, var address:String,var customer:String)
