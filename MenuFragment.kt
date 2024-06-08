package com.example.chucksgourmet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuFragment : Fragment() {
    lateinit var btnOrderHistory:ImageButton
    lateinit var btnLogout:ImageButton
    lateinit var btnCart:ImageButton
    lateinit var viewPager: ViewPager2
    lateinit var featuredMenuRV:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        initUI(view)
        handleEvents(view)
        return view
    }

    private fun handleEvents(view:View) {
        btnLogout.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            // Log out the user
            if (auth.currentUser != null) {
                auth.signOut()
            }
            it.findNavController().navigate(R.id.action_menuFragment_to_loginFragment)
        }
        btnOrderHistory.setOnClickListener {
            it.findNavController().navigate(R.id.action_menuFragment_to_orderHistoryFragment)
        }
        btnCart.setOnClickListener {
            it.findNavController().navigate(R.id.action_menuFragment_to_viewCartFragment)
        }
    }

    private fun initUI(view: View) {
        btnLogout=view.findViewById(R.id.imgBtnLogout)
        btnOrderHistory = view.findViewById(R.id.imgNavOrderHistory)
        btnCart = view.findViewById(R.id.imgNavCart)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        featuredMenuRV = view.findViewById(R.id.rvFeaturedItems)
        val promotionItems = ArrayList<MenuItemDC>()

        val db = FirebaseFirestore.getInstance()
        db.collection("menuItem")
            .get()
            .addOnSuccessListener { documents ->
                val menuItems = ArrayList<ArrayList<MenuItemDC>>()
                // Initialize ArrayLists for each itemType
                val chickenItems = ArrayList<MenuItemDC>()
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

                    // Add menu items which are having promotion
                    if (itemPromotion!!>0.0){
                        promotionItems.add(menuItem)
                        Log.d("MYTAG","I added the menu item"+promotionItems.size.toString())
                    }

                    // Add the MenuItemDC object to the appropriate ArrayList based on itemType
                    when (itemType) {
                        "chicken" -> chickenItems.add(menuItem)
                        "sides" -> sideItems.add(menuItem)
                        "drinks" -> drinkItems.add(menuItem)
                    }
                }
                //Featured Item Recycle view
                featuredMenuRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
                val adapter = FeaturedMenuRVAdapter(requireActivity() as AppCompatActivity,promotionItems)
                featuredMenuRV.adapter = adapter

                // Add the ArrayLists to menuItems
                menuItems.add(chickenItems)
                menuItems.add(sideItems)
                menuItems.add(drinkItems)

                // Set up ViewPager and TabLayout after fetching menu items
                viewPager.adapter = ViewPagerAdapter(requireActivity() as AppCompatActivity, menuItems,null)

                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    when (position) {
                        0 -> tab.text = "Chicken"
                        1 -> tab.text = "Sides"
                        2 -> tab.text = "Drinks"
                    }
                }.attach()
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.e("MenuFragment", "Error getting documents: ", exception)
            }
    }
}