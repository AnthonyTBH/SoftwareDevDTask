package com.example.chucksgourmet

import NotificationDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ViewCartFragment : Fragment() , CartRVAdapter.OnCartItemClickListener{
    private lateinit var cartViewModel: CartViewModel
    private lateinit var viewModel: MyViewModel

    private var param1: String? = null
    private var param2: String? = null
    lateinit var btnLogout:ImageButton
    lateinit var btnOrderHistory: ImageButton
    lateinit var btnMenu: ImageButton
    lateinit var btnConfirmOrder: Button
    lateinit var rvOrder:RecyclerView
    lateinit var total:TextView
    lateinit var etDate:TextView
    lateinit var etTime:TextView
    lateinit var location:Spinner
    lateinit var address:TextView
    lateinit var selectedDateTime: Calendar

    private lateinit var cartAdapter: CartRVAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onMenuItemClick(menuItem: MenuItemDC) {
        CartManager.removeItem(menuItem)
        Log.d("MYTAG","removed")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_cart, container, false)
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)

        initUI(view)
        // Set initial values or observe LiveData (optional)
        etTime.setText(viewModel.time.value ?: "")
        etDate.setText(viewModel.date.value ?: "")
        handleEvents(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewCartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun initUI(view: View) {
        btnLogout=view.findViewById(R.id.imgBtnLogout)
        btnMenu=view.findViewById(R.id.imgNavMenu)
        btnOrderHistory=view.findViewById(R.id.imgNavOrderHistory)
        etDate = view.findViewById(R.id.etViewCartDate)
        etTime = view.findViewById(R.id.etViewCartTime)
        rvOrder = view.findViewById(R.id.rvCartItems)
        rvOrder.layoutManager = LinearLayoutManager(context)
        rvOrder.adapter = CartRVAdapter(CartManager.getItems(),cartViewModel)
        // Initialize and set up the adapter
        cartAdapter = CartRVAdapter(emptyList(),cartViewModel) // Pass an empty list initially
        rvOrder.adapter = cartAdapter

        // Observe changes to the cartItems LiveData in the ViewModel
        cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            // Update the RecyclerView adapter with the new list of cart items
            cartAdapter.setItems(cartItems)
        })

        btnConfirmOrder = view.findViewById(R.id.btnViewCartConfirmOrder)
        total = view.findViewById(R.id.tvViewCardTotal)
        location = view.findViewById(R.id.spinnerViewCartLocation)
        address = view.findViewById(R.id.spinnerViewCartAddress)

        //fetch address
        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

        // Fetch locations from Firestore
        db.collection("locations")
            .get()
            .addOnSuccessListener { documents ->
                val locations = ArrayList<String>()
                val addresses = ArrayList<String>()

                for (document in documents) {
                    val location = document.getString("name")
                    val address = document.getString("address")
                    if (location != null && address != null) {
                        locations.add(location)
                        addresses.add(address)
                    }
                }

                // Create an ArrayAdapter using the location names
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                location.adapter = adapter

                // Set an OnItemSelectedListener for the spinner
                location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        // Update the address TextView with the selected location's address
                        address.text = "Address: ${addresses[position]}"
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }
    private fun handleEvents(view: View) {
        btnLogout.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            // Log out the user
            if (auth.currentUser != null) {
                auth.signOut()
            }
            it.findNavController().navigate(R.id.action_viewCartFragment_to_loginFragment)
        }
        btnMenu.setOnClickListener {
            it.findNavController().navigate(R.id.action_viewCartFragment_to_menuFragment)
        }
        btnOrderHistory.setOnClickListener {
            it.findNavController().navigate(R.id.action_viewCartFragment_to_orderHistoryFragment)
        }
        // Update LiveData when user edits the EditTexts
        etTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setTime(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })

        etDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setDate(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        btnConfirmOrder.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                it.findNavController().navigate(R.id.action_viewCartFragment_to_loginFragment)
            }
            else {
                if (etDate.text.toString() == "") {
                    val notificationDialog =
                        NotificationDialog(requireActivity(), "Please enter a date!")
                    notificationDialog.show()
                }
                else if(etTime.text.toString()==""){
                    val notificationDialog = NotificationDialog(requireActivity(),"Please enter a time!")
                    notificationDialog.show()
                }
              else if(validateTime(etDate.text.toString(),etTime.text.toString())) {
                    val cartItems = CartManager.getItems()
                    val quantitiesMap = mutableMapOf<Any, Int>() // Create a map to store quantities

                    for (item in cartItems) {
                        if (item.menuItem != null) {
                            val quantity = item.quantities[item.menuItem] ?: 0 // Get the current quantity or default to 0
                            quantitiesMap[item.menuItem] = quantity  // Increment the quantity for the menuItem
                        }
                        if (item.menuSet != null) {
                            val quantity = item.quantities[item.menuSet] ?: 0 // Get the current quantity or default to 0
                            quantitiesMap[item.menuSet] = quantity  // Increment the quantity for the menuSet
                        }
                    }

                    if (cartItems.isEmpty()) {
                        val notificationDialog = NotificationDialog(requireActivity(), "Your cart is empty!")
                        notificationDialog.show()
                    } else {
                        // Get the Firestore instance
                        val db = FirebaseFirestore.getInstance()

                        val ordersCollectionRef = db.collection("orders")
                        val statuses = listOf("pending", "preparing", "ready for pickup")
                        val pendingOrdersQuery = ordersCollectionRef.whereIn("orderStatus", statuses)

                        pendingOrdersQuery.get()
                            .addOnSuccessListener { documents ->
                                val hasPendingOrders = !documents.isEmpty

                                if (hasPendingOrders) {
                                    val notificationDialog = NotificationDialog(
                                        requireActivity(),
                                        "Sorry, you currently have an order in progress, to prevent spam orders only one order can be made at a time"
                                    )
                                    notificationDialog.show()
                                } else {
                                    // Create an Order object
                                    val order = Order(
                                        quantitiesMap.keys.filterIsInstance<MenuItemDC>().toMutableList() as ArrayList<MenuItemDC>,
                                        quantitiesMap.filterKeys { it is MenuItemDC }.values.toList().toMutableList() as ArrayList<Int>,
                                        quantitiesMap.keys.filterIsInstance<MenuSetDC>().toMutableList() as ArrayList<MenuSetDC>,
                                        quantitiesMap.filterKeys { it is MenuSetDC }.values.toList().toMutableList() as ArrayList<Int>,
                                        CartManager.getTotalPrice(),
                                        etDate.text.toString(),
                                        etTime.text.toString(),
                                        "pending",
                                        location.selectedItem.toString(),
                                        address.text.toString(),
                                        FirebaseAuth.getInstance().currentUser?.email.toString()
                                    )
                                    Log.d("MYTAG","order is "+order.toString())

                                    // Insert the order into the "orders" collection
                                    insertOrder(order)

                                    // Show a notification dialog
                                    val notificationDialog = NotificationDialog(
                                        requireActivity(),
                                        "Your order has been placed!"
                                    )
                                    notificationDialog.show()

                                    // Clear the cart
                                    CartManager.clearCart()

                                    // Update the RecyclerView adapter
                                    cartAdapter.setItems(emptyList())
                                    cartAdapter.notifyDataSetChanged()

                                    // Set up the RecyclerView with the new list of cart items
                                    rvOrder.layoutManager = LinearLayoutManager(context)
                                    rvOrder.adapter = CartRVAdapter(CartManager.getItems(), cartViewModel)

                                    // Observe changes in cart items
                                    cartViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
                                        // Update the RecyclerView adapter with the new list of cart items
                                        cartAdapter.setItems(cartItems)
                                    })

                                    // Clear date and time fields
                                    viewModel.setTime("")
                                    viewModel.setDate("")
                                    etTime.setText("")
                                    etDate.setText("")
                                    total.setText(String.format("%.2f",0.0))
                                    MyFirebaseMessagingService.sendNotificationToDevice(requireContext(),"Order Created","A new order has been created, check the app for more info")
                                }
                            }
                    }

                }
            }
        }
        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            total.text = String.format("%.2f",totalPrice)
        }
        etDate.setOnClickListener {
            showDatePicker()
        }
        etTime.setOnClickListener {
            showTimePicker()
        }
    }
    private fun showDatePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val minDate = Calendar.getInstance()
        minDate.set(currentYear, currentMonth, currentDay)

        DatePickerDialog(requireActivity(), { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }, currentYear, currentMonth, currentDay).apply {
            datePicker.minDate = minDate.timeInMillis
        }.show()
    }

    private fun showTimePicker() {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(requireActivity(), { _, hourOfDay, minute ->
            selectedDateTime = Calendar.getInstance()
            selectedDateTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            selectedDateTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            selectedDateTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedDateTime.set(Calendar.MINUTE, minute)
            selectedDateTime.set(Calendar.SECOND, 0)
            selectedDateTime.set(Calendar.MILLISECOND, 0)

            val currentDateTime = Calendar.getInstance()
            currentDateTime.add(Calendar.MINUTE, 30)

            if (selectedDateTime.before(currentDateTime)) {
                // Selected time is less than 30 minutes from the current time
                val notificationDialog = NotificationDialog(requireActivity(),"Your order must be at least 30 minutes in advance")
                notificationDialog.show()
            } else {
                // Update the calendar with the selected date and time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeInView()
            }
        }, currentHour, currentMinute, true)

        timePickerDialog.show()
    }
    private fun validateTime(currentDateString: String, currentTimeString: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            val selectedDate = dateFormat.parse(currentDateString) ?: return false
            val currentTime = currentTimeFormat.parse(currentTimeString) ?: return false

            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            val selectedDateTime = Calendar.getInstance()
            selectedDateTime.time = currentTime
            selectedDateTime.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            selectedDateTime.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            selectedDateTime.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))

            val thresholdDateTime = Calendar.getInstance()
            thresholdDateTime.add(Calendar.MINUTE, 30)
            if(!selectedDateTime.after(thresholdDateTime)){
                val notificationDialog = NotificationDialog(requireActivity(),"Your order must be at least 30 minutes in advance")
                notificationDialog.show()
            }
            return selectedDateTime.after(thresholdDateTime)
        } catch (e: ParseException) {
            // Handle invalid date or time format
            e.printStackTrace()
            return false
        }
    }

    private fun updateDateInView() {
        // Update the date view (e.g., a TextView) with the selected date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.text = dateFormat.format(calendar.time)
    }

    private fun updateTimeInView() {
        // Update the time view (e.g., a TextView) with the selected time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        etTime.text = timeFormat.format(calendar.time)
    }


    // Define a function to insert the order into Firestore
    fun insertOrder(order: Order) {
        // Get a reference to your Firestore database
        val db = FirebaseFirestore.getInstance()
        // Create a new document in your "orders" collection
        db.collection("orders")
            .add(order.toMap())
            .addOnSuccessListener { documentReference ->
                // Document successfully added
                println("Order added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error handling
                println("Error adding order: $e")
            }
    }

    // Extension function to convert Order object to Map
    fun Order.toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["orderedItems"] = orderedItems.map { it.toMap() } // Convert MenuItemDC objects to Map
        map["orderItemQuantity"] = orderItemQuantity
        map["orderedSets"] = orderedSets.map { it.toMap() } // Convert MenuSetDC objects to Map
        map["orderSetQuantity"] = orderSetQuantity
        map["total"] = total
        map["orderDate"] = orderDate
        map["orderTime"] = orderTime
        map["orderStatus"] = orderStatus
        map["location"] = location
        map["address"] = address
        map["user"] = customer
        return map
    }

    // Extension function to convert MenuItemDC object to Map
    fun MenuItemDC.toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["itemType"] = itemType
        map["itemName"] = itemName
        map["itemPrice"] = itemPrice
        map["imageURL"] = imageURL
        map["itemPromotion"] = itemPromotion
        return map
    }

    // Extension function to convert MenuSetDC object to Map
    fun MenuSetDC.toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["setName"] = setName
        map["setPrice"] = setPrice
        map["menuItems"] = menuItems.map { it.toMap() } // Convert list of MenuItemDC objects to List<Map<String, Any>>
        map["setQuantity"] = quantity
        map["menuItemQuantity"] = menuItemQuantities
        return map
    }


}
