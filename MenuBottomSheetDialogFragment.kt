package com.example.chucksgourmet

import NotificationDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore

class MenuBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var menuItem: MenuItemDC
    private lateinit var imgMenuItem: ImageView
    private lateinit var tvMenuItemName: TextView
    private lateinit var tvMenuItemPrice: TextView
    private lateinit var menuItemQuantity:TextView
    private lateinit var btnMenuItemMinusQuantity: Button
    private lateinit var btnMenuItemAddQuantity: Button
    private lateinit var btnSubmit:Button
    private lateinit var rvSides:RecyclerView
    private lateinit var rvDrinks:RecyclerView
    private lateinit var cartViewModel: CartViewModel
    private lateinit var tvSelectedMenuItemSideCount:TextView
    private lateinit var tvSelectedMenuItemDrinkCount:TextView

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
        val view = inflater.inflate(R.layout.menu_dialog_box, container, false)
        initUI(view)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
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
            if(menuItemQuantity.text.toString().toInt()>0){
                checkSideAndDrinkQuantities()
                dismiss()
            }
            else{
                val notificationDialog = NotificationDialog(requireActivity(),"You didn't order a main dish!")
                notificationDialog.show()
            }
        }
    }

    fun checkSideAndDrinkQuantities() {
        val drinkAdapter = rvDrinks.adapter as? DialogBoxMenuItemRVAdapter
        val drinkItems = drinkAdapter?.getSelectedItems() ?: emptyList()
        val sideAdapter = rvSides.adapter as? DialogBoxMenuItemRVAdapter
        val sideItems = sideAdapter?.getSelectedItems() ?: emptyList()

        val noDrinkItems = drinkItems.isEmpty()
        val noSideItems = sideItems.isEmpty()

        if (noDrinkItems && noSideItems) {
            // No sides and drinks selected
            cartViewModel.addItem(menuItem, null, menuItemQuantity.text.toString().toInt())
            val notificationDialog = NotificationDialog(requireActivity(), "Order added to cart!")
            notificationDialog.show()
        } else if (noDrinkItems || noSideItems) {
            // Either a side or a drink is missing
            val notificationDialog = NotificationDialog(requireActivity(), "A side and a drink must be selected to count as a meal, therefore the orders made will count as a separate order")
            notificationDialog.show()
            cartViewModel.addItem(menuItem, null, menuItemQuantity.text.toString().toInt())
            if (noSideItems) {
                for (drinkItem in drinkItems) {
                    cartViewModel.addItem(drinkItem, null, menuItemQuantity.text.toString().toInt())
                }
            } else {
                for (sideItem in sideItems) {
                    cartViewModel.addItem(sideItem, null, menuItemQuantity.text.toString().toInt())
                }
            }
        } else {
            // Both sides and drinks are selected
            val listOfMenuItems = ArrayList<MenuItemDC>().apply {
                add(menuItem)
                addAll(sideItems)
                addAll(drinkItems)
            }

            val uniqueItemsWithCounts = countMenuItemsWithCounts(listOfMenuItems)

            val menuItemPrice = menuItem.itemPrice * (1 - menuItem.itemPromotion)
            val sideItemsPrice = sideItems.sumOf { it.itemPrice * (1 - it.itemPromotion) }
            val drinkItemsPrice = drinkItems.sumOf { it.itemPrice * (1 - it.itemPromotion) }
            val finalPrice = menuItemPrice + sideItemsPrice + drinkItemsPrice

            val menuSet = MenuSetDC(
                "${menuItem.itemName} Set",
                finalPrice,
                menuItemQuantity.text.toString().toInt(),
                ArrayList(uniqueItemsWithCounts.keys),
                ArrayList(uniqueItemsWithCounts.values)
            )
            Log.d("MYTAG", "here $menuSet")
            cartViewModel.addItem(null, menuSet, menuSet.quantity)

            val notificationDialog = NotificationDialog(requireActivity(), "Order added to cart!")
            notificationDialog.show()
        }
    }

    fun countMenuItemsWithCounts(menuItems: ArrayList<MenuItemDC>): LinkedHashMap<MenuItemDC, Int> {
        val itemCountMap = LinkedHashMap<MenuItemDC, Int>()

        for (menuItem in menuItems) {
            itemCountMap[menuItem] = itemCountMap.getOrDefault(menuItem, 0) + 1
        }

        return itemCountMap
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
        tvMenuItemPrice.setText(String.format("%.2f",menuItem.itemPrice-(menuItem.itemPrice*menuItem.itemPromotion)))
        menuItemQuantity= view.findViewById(R.id.tvSelectedMenuItemQuantityValue)
        btnMenuItemMinusQuantity= view.findViewById(R.id.btnSelectedMenuItemMinus)
        btnMenuItemAddQuantity = view.findViewById(R.id.btnSelectedMenuItemPlus)
        btnSubmit = view.findViewById(R.id.btnAddSelectedMenuItemToCart)
        tvSelectedMenuItemSideCount = view.findViewById(R.id.tvSelectedMenuItemSideCount)
        tvSelectedMenuItemDrinkCount = view.findViewById(R.id.tvSelectedMenuItemDrinkCount)
        when {
             menuItem.itemPrice * (1 - menuItem.itemPromotion)>= 35 -> {
                 tvSelectedMenuItemSideCount.setText("x2")
                 tvSelectedMenuItemDrinkCount.setText("x2")
            }
             menuItem.itemPrice * (1 - menuItem.itemPromotion)>= 25 -> {
                 tvSelectedMenuItemSideCount.setText("x1")
                 tvSelectedMenuItemDrinkCount.setText("x2")
            }
             menuItem.itemPrice * (1 - menuItem.itemPromotion)>= 15 -> {
                 tvSelectedMenuItemSideCount.setText("x1")
                 tvSelectedMenuItemDrinkCount.setText("x1")
            }
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("menuItem")
            .get()
            .addOnSuccessListener { documents ->
                val drinkItems = ArrayList<MenuItemDC>()
                val sideItems = ArrayList<MenuItemDC>()

                for (document in documents) {
                    val itemType = document.getString("itemType")
                    val itemName = document.getString("itemName")
                    val itemPrice = document.getString("itemPrice")?.toDouble()
                    val imageURL = document.getString("imageUrl")
                    val itemPromotion = document.getString("itemPromotion")?.toDouble()

                    // Create a MenuItemDC object
                    val menuItem = MenuItemDC(itemType!!, itemName!!, itemPrice!!, imageURL!!, itemPromotion ?: 0.0)

                    // Add the MenuItemDC object to the appropriate ArrayList based on itemType
                    when (itemType) {
                        "sides" -> sideItems.add(menuItem)
                        "drinks" -> drinkItems.add(menuItem)
                    }
                }
                rvDrinks = view.findViewById(R.id.rvSelectedMenuItemDrinks)
                rvDrinks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                val drinkAdapter = DialogBoxMenuItemRVAdapter(requireActivity() as AppCompatActivity, drinkItems,menuItem.itemPrice * (1 - menuItem.itemPromotion))
                rvDrinks.adapter = drinkAdapter

                // Set up RecyclerView adapters after populating the lists
                rvSides = view.findViewById(R.id.rvSelectedMenuItemSides)
                rvSides.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                val sideAdapter = DialogBoxMenuItemRVAdapter(requireActivity() as AppCompatActivity, sideItems,menuItem.itemPrice * (1 - menuItem.itemPromotion))
                rvSides.adapter = sideAdapter
            }
    }
}
