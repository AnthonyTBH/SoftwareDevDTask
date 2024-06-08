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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminMenuFragment : Fragment() {
    lateinit var btnAdminOrder: ImageButton
    lateinit var btnLogout: ImageButton
    lateinit var btnAnalytics: ImageButton
    lateinit var viewPager: ViewPager2
    lateinit var featuredMenuRV: RecyclerView
    lateinit var fab: FloatingActionButton
    lateinit var tabLayout: TabLayout
    lateinit var myView:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_admin_menu, container, false)
        myView = view
        initUI(view)
        handleEvents(view)
        getMenuItems()
        return view
    }


    private fun handleEvents(view:View) {
        btnLogout.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            // Log out the user
            if (auth.currentUser != null) {
                auth.signOut()
            }
            it.findNavController().navigate(R.id.action_adminMenuFragment_to_loginFragment)
        }
        btnAdminOrder.setOnClickListener {
            it.findNavController().navigate(R.id.action_adminMenuFragment_to_adminOrderFragment)
        }
        btnAnalytics.setOnClickListener {
            it.findNavController().navigate(R.id.action_adminMenuFragment_to_adminDashboardFragment)
        }
        fab.setOnClickListener {
            val createMenuFragment = CreateMenuFragment()
            createMenuFragment.show(childFragmentManager, "CreateMenuFragment")
        }
    }

    private fun initUI(view: View) {
        btnLogout=view.findViewById(R.id.imgBtnLogout)
        btnAdminOrder = view.findViewById(R.id.imgNavOrderHistory)
        fab = view.findViewById(R.id.fab)

        btnAnalytics = view.findViewById(R.id.imgNavAnalytics)
        tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        featuredMenuRV = view.findViewById(R.id.rvFeaturedItems)

    }

    fun getMenuItems() {
        val promotionItems = ArrayList<MenuItemDC>()
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("menuItem")

        // Add a snapshot listener to listen for real-time changes
        val listenerRegistration = collectionRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("AdminMenuFragment", "Listen failed", e)
                return@addSnapshotListener
            }

            val menuItems = ArrayList<ArrayList<MenuItemDC>>()
            val chickenItems = ArrayList<MenuItemDC>()
            val drinkItems = ArrayList<MenuItemDC>()
            val sideItems = ArrayList<MenuItemDC>()
            promotionItems.clear()

            for (document in snapshots!!) {
                val itemType = document.getString("itemType")
                val itemName = document.getString("itemName")
                val itemPrice = document.getString("itemPrice")?.toDouble()
                val imageURL = document.getString("imageUrl")
                val itemPromotion = document.getString("itemPromotion")?.toDouble()

                // Create a MenuItemDC object
                val menuItem = MenuItemDC(itemType!!, itemName!!, itemPrice!!, imageURL!!, itemPromotion ?: 0.0)

                // Add menu items which are having promotion
                if (itemPromotion!! > 0.0) {
                    promotionItems.add(menuItem)
                    Log.d("MYTAG", "I added the menu item" + promotionItems.size.toString())
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
            val adapter = FeaturedMenuRVAdapter(requireActivity() as AppCompatActivity,promotionItems,true, fragmentManager = childFragmentManager)
            featuredMenuRV.adapter = adapter

            // Add the ArrayLists to menuItems
            menuItems.add(chickenItems)
            menuItems.add(sideItems)
            menuItems.add(drinkItems)

            // Set up ViewPager and TabLayout after fetching menu items
            viewPager.adapter = ViewPagerAdapter(requireActivity() as AppCompatActivity, menuItems,true)

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Chicken"
                    1 -> tab.text = "Sides"
                    2 -> tab.text = "Drinks"
                }
            }.attach()
        }
    }



}
