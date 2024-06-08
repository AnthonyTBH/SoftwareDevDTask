package com.example.chucksgourmet

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.AxisValue
import lecho.lib.hellocharts.model.Line
import lecho.lib.hellocharts.model.LineChartData
import lecho.lib.hellocharts.model.PointValue
import lecho.lib.hellocharts.view.LineChartView
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AdminDashboardFragment : Fragment(), OnMapReadyCallback{

    private lateinit var chartView: LineChartView
    private lateinit var spinnerTimeRange: Spinner
    private lateinit var imgMostPopularChicken: ImageView
    private lateinit var tvMostPopularChicken: TextView
    private lateinit var imgMostPopularSide: ImageView
    private lateinit var tvMostPopularSide: TextView
    private lateinit var imgMostPopularDrink: ImageView
    private lateinit var tvMostPopularDrink: TextView
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var etLocationName: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnGenerateAddress: Button
    private lateinit var btnSubmit: Button
    private lateinit var scrollView: ScrollView
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var btnLogout: ImageButton
    private lateinit var btnAdminOrder:ImageButton
    private lateinit var btnAdminMenu:ImageButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
        initUI(view)
        handleEvents(view)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        MapsInitializer.initialize(requireContext())
        mapView.getMapAsync(this)

        Places.initialize(requireContext(), "api key")

        return view
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        fetchLocations()
    }
    private fun handleEvents(view:View) {
        btnLogout.setOnClickListener {
            val auth = FirebaseAuth.getInstance()

            // Log out the user
            if (auth.currentUser != null) {
                auth.signOut()
            }
            it.findNavController().navigate(R.id.action_adminDashboardFragment_to_loginFragment)
        }
        btnAdminOrder.setOnClickListener {
            it.findNavController().navigate(R.id.action_adminDashboardFragment_to_adminOrderFragment)
        }
        btnAdminMenu.setOnClickListener {
            it.findNavController().navigate(R.id.action_adminDashboardFragment_to_adminMenuFragment)
        }
    }
    private fun fetchLocations() {
        val db = FirebaseFirestore.getInstance()
        db.collection("locations")
            .get()
            .addOnSuccessListener { result ->
                val boundsBuilder = LatLngBounds.Builder()
                val geocoder = Geocoder(requireContext()) // Initialize Geocoder

                for (document in result) {
                    val locationName = document.getString("name") ?: "Unknown location"
                    val address = document.getString("address") ?: "Unknown address"

                    try {
                        // Geocode the address to obtain its coordinates
                        val addresses = geocoder.getFromLocationName(address, 1)
                        if (addresses != null) {
                            if (addresses.isNotEmpty()) {
                                val lat = addresses[0].latitude
                                val lng = addresses[0].longitude
                                val latLng = LatLng(lat, lng)

                                val markerOptions = MarkerOptions()
                                    .position(latLng)
                                    .title(locationName)
                                    .snippet(address)

                                googleMap.addMarker(markerOptions)
                                boundsBuilder.include(latLng)
                            } else {
                                // Handle case where address couldn't be geocoded
                            }
                        }
                    } catch (e: IOException) {
                        // Handle geocoding errors
                        e.printStackTrace()
                    }
                }

                val bounds = boundsBuilder.build()
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun initUI(view: View) {
        btnLogout = view.findViewById(R.id.imgBtnLogout)
        btnAdminOrder = view.findViewById(R.id.imgNavOrderAdmin)
        btnAdminMenu = view.findViewById(R.id.imgNavMenu)
        spinnerTimeRange = view.findViewById(R.id.spinnerTimeRange)
        val timeRangeOptions = arrayOf("Last 7 days", "Last 14 days", "Last month")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeRangeOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTimeRange.adapter = spinnerAdapter
        spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> fetchOrdersData(7) // Fetch data for last 7 days
                    1 -> fetchOrdersData(14) // Fetch data for last 14 days
                    2 -> fetchOrdersData(30) // Fetch data for last month
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        chartView = view.findViewById(R.id.chart)

        imgMostPopularChicken = view.findViewById(R.id.imgMostPopularChicken)
        tvMostPopularChicken = view.findViewById(R.id.tvMostPopularChicken)
        imgMostPopularSide = view.findViewById(R.id.imgMostPopularSide)
        tvMostPopularSide = view.findViewById(R.id.tvMostPopularSide)
        imgMostPopularDrink = view.findViewById(R.id.imgMostPopularDrink)
        tvMostPopularDrink = view.findViewById(R.id.tvMostPopularDrink)

        fetchMostPopularItems()

        etLocationName = view.findViewById(R.id.etLocationName)
        etAddress = view.findViewById(R.id.etAddress)
        btnGenerateAddress = view.findViewById(R.id.btnGenerateAddress)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        btnGenerateAddress.setOnClickListener {
            openPlacePicker()
        }

        btnSubmit.setOnClickListener {
            val locationName = etLocationName.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (locationName.isNotEmpty() && address.isNotEmpty()) {
                // Add the location to Firestore
                addLocationToFirestore(locationName, address)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        scrollView = view.findViewById(R.id.scrollView)
    }
    private fun openPlacePicker() {

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                etAddress.setText(place.address)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Toast.makeText(requireContext(), "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    private fun addLocationToFirestore(locationName: String, address: String) {
        // Create a new document in the "locations" collection with the provided data
        val location = hashMapOf(
            "name" to locationName,
            "address" to address
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("locations")
            .add(location)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Location added successfully", Toast.LENGTH_SHORT).show()
                // Clear input fields after successful submission
                etLocationName.text.clear()
                etAddress.text.clear()
                fetchLocations()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun fetchOrdersData(days: Int) {
        val db = FirebaseFirestore.getInstance()
        val ordersCollectionRef = db.collection("orders")

        // Get the current date
        val currentDate = Calendar.getInstance()

        // Set the start date for the query (e.g., 30 days ago from the current date)
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.DAY_OF_YEAR, -days) // Adjust the number of days as needed

        // Firestore query to fetch orders within the last 30 days
        ordersCollectionRef
            .whereEqualTo("orderStatus", "completed") // Assuming completed orders are stored with status "completed"
            .get()
            .addOnSuccessListener { documents ->
                val ordersData = mutableListOf<Int>()

                // Initialize the list with 30 elements (each representing a day)
                for (i in 1..30) {
                    ordersData.add(0)
                }

                // Loop through the documents to aggregate orders per day
                for (document in documents) {
                    val orderDateString = document.getString("orderDate")
                    val orderDate = orderDateString?.toDate()
                    val daysAgo = getDaysAgo(orderDate)
                    if (daysAgo != null && daysAgo >= 0 && daysAgo < days) {
                        ordersData[daysAgo]++
                    }
                }

                // Populate line chart with fetched data
                populateLineChart(ordersData)
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

    private fun String.toDate(): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
        } catch (e: ParseException) {
            null
        }
    }

    private fun getDaysAgo(date: Date?): Int? {
        if (date == null) return null
        val currentDate = Calendar.getInstance()
        val orderDate = Calendar.getInstance().apply { time = date }
        val diff = currentDate.timeInMillis - orderDate.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt() // Convert milliseconds to days
    }
    private fun populateLineChart(ordersCount: List<Int>) {
        val correctedList = ordersCount.reversed()
        val lineEntries = mutableListOf<PointValue>()
        for (i in correctedList.indices) {
            lineEntries.add(PointValue(i.toFloat(), correctedList[i].toFloat()))
        }

        val line = Line(lineEntries)
            .setColor(Color.BLUE)
            .setFilled(true)
            .setHasPoints(true)

        val lineChartData = LineChartData()
        lineChartData.lines.add(line)

        // Define Y axis
        val yAxis = Axis().apply {
            name = "Orders Count"
            textColor = Color.BLACK
            lineColor = Color.BLACK
            values = (0 until correctedList.maxOrNull()!! + 1).map {
                AxisValue(it.toFloat()).setLabel(it.toString())
            }
        }

        lineChartData.axisYLeft = yAxis

        chartView.lineChartData = lineChartData
    }
    private fun fetchMostPopularItems() {
        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                val itemCount = mutableMapOf<String, MutableMap<String, Int>>(
                    "chicken" to mutableMapOf(),
                    "sides" to mutableMapOf(),
                    "drinks" to mutableMapOf()
                )

                val itemDetails = mutableMapOf<String, MutableMap<String, Pair<String, String>>>(
                    "chicken" to mutableMapOf(),
                    "sides" to mutableMapOf(),
                    "drinks" to mutableMapOf()
                )

                for (document in result) {
                    // Process orderedItems
                    val orderedItems = document.get("orderedItems") as? List<Map<String, Any>> ?: emptyList()
                    for (item in orderedItems) {
                        val itemType = item["itemType"] as String
                        val itemName = item["itemName"] as String
                        val imageURL = item["imageURL"] as String

                        if (itemType in itemCount) {
                            val typeCount = itemCount[itemType]!!
                            if (typeCount.containsKey(itemName)) {
                                typeCount[itemName] = typeCount[itemName]!! + 1
                            } else {
                                typeCount[itemName] = 1
                                itemDetails[itemType]!![itemName] = Pair(itemName, imageURL)
                            }
                        }
                    }

                    // Process orderedSets
                    val orderedSets = document.get("orderedSets") as? List<Map<String, Any>> ?: emptyList()
                    for (set in orderedSets) {
                        val menuItems = set["menuItems"] as? List<Map<String, Any>> ?: emptyList()
                        for (item in menuItems) {
                            val itemType = item["itemType"] as String
                            val itemName = item["itemName"] as String
                            val imageURL = item["imageURL"] as String

                            if (itemType in itemCount) {
                                val typeCount = itemCount[itemType]!!
                                if (typeCount.containsKey(itemName)) {
                                    typeCount[itemName] = typeCount[itemName]!! + 1
                                } else {
                                    typeCount[itemName] = 1
                                    itemDetails[itemType]!![itemName] = Pair(itemName, imageURL)
                                }
                            }
                        }
                    }
                }
                Log.d("MYTAG", itemDetails.toString())
                displayMostPopularItem(itemCount, itemDetails, "chicken", tvMostPopularChicken, imgMostPopularChicken)
                displayMostPopularItem(itemCount, itemDetails, "sides", tvMostPopularSide, imgMostPopularSide)
                displayMostPopularItem(itemCount, itemDetails, "drinks", tvMostPopularDrink, imgMostPopularDrink)
            }
    }


    private fun displayMostPopularItem(
        itemCount: Map<String, Map<String, Int>>,
        itemDetails: Map<String, Map<String, Pair<String, String>>>,
        itemType: String,
        textView: TextView,
        imageView: ImageView
    ) {
        val mostPopularItem = itemCount[itemType]?.maxByOrNull { it.value }?.key

        if (mostPopularItem != null) {
            val mostPopularDetails = itemDetails[itemType]?.get(mostPopularItem)
            if (mostPopularDetails != null) {
                textView.text = mostPopularDetails.first
                Glide.with(this)
                    .load(mostPopularDetails.second)
                    .into(imageView)
            }
        }
    }
}

