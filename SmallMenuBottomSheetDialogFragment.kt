package com.example.chucksgourmet

import NotificationDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SmallMenuBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var menuItem: MenuItemDC
    private lateinit var imgMenuItem: ImageView
    private lateinit var tvMenuItemName: TextView
    private lateinit var tvMenuItemPrice: TextView
    private lateinit var menuItemQuantity:TextView
    private lateinit var btnMenuItemMinusQuantity: Button
    private lateinit var btnMenuItemAddQuantity: Button
    private lateinit var btnSubmit:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            menuItem = it.getParcelable<MenuItemDC>("menuItem") as MenuItemDC
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.small_menu_dialog_box, container, false)
        initUI(view)
        handleEvent()
        return view;
    }

    private fun handleEvent() {
        btnMenuItemMinusQuantity.setOnClickListener {
            val currentQuantity = menuItemQuantity.text.toString().toInt()
            if (currentQuantity > 0) {
                menuItemQuantity.setText((currentQuantity - 1).toString())
            }
        }

        btnMenuItemAddQuantity.setOnClickListener {
            val currentQuantity = menuItemQuantity.text.toString().toInt()
            menuItemQuantity.setText((currentQuantity + 1).toString())
        }
        btnSubmit.setOnClickListener{
            CartManager.addItem(menuItem,null,menuItemQuantity.text.toString().toInt())
            dismiss()
            val notificationDialog = NotificationDialog(requireActivity(),"Order added to cart!")
            notificationDialog.show()
        }
    }

    private fun initUI(view:View) {
        imgMenuItem = view.findViewById(R.id.imgSelectedMenuItem)
        Glide.with(this)
            .load(menuItem.imageURL)
            .placeholder(R.drawable.cooked_chicken)
            .into(imgMenuItem)
        tvMenuItemName = view.findViewById(R.id.tvSelectedMenuItemName)
        tvMenuItemName.setText(menuItem.itemName)
        tvMenuItemPrice = view.findViewById(R.id.tvSelectedMenuItemPrice)
        tvMenuItemPrice.setText(String.format("%.2f",menuItem.itemPrice))
        menuItemQuantity= view.findViewById(R.id.tvSelectedMenuItemQuantityValue)
        btnMenuItemMinusQuantity= view.findViewById(R.id.btnSelectedMenuItemMinus)
        btnMenuItemAddQuantity = view.findViewById(R.id.btnSelectedMenuItemPlus)
        btnSubmit = view.findViewById(R.id.btnAddSelectedMenuItemToCart)
    }
}
