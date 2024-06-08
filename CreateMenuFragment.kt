package com.example.chucksgourmet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateMenuFragment(private val menuItem: MenuItemDC? = null,private val isFeaturedMenuItem:Boolean?=null) : BottomSheetDialogFragment() {

    private lateinit var imageView: ImageView
    private lateinit var buttonUpload: Button
    private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextPromotion: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var sliderPromotion: SeekBar
    private lateinit var createUpdateHeader:TextView
    private lateinit var imageUri: Uri
    private val PICK_IMAGE_REQUEST = 1

    private val storageReference: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_menu, container, false)

        imageView = view.findViewById(R.id.imageView)
        buttonUpload = view.findViewById(R.id.buttonUpload)
        editTextName = view.findViewById(R.id.editTextName)
        editTextPrice = view.findViewById(R.id.editTextPrice)
        editTextPromotion = view.findViewById(R.id.editTextPromotion)
        spinnerType = view.findViewById(R.id.spinnerType)
        sliderPromotion = view.findViewById(R.id.sliderPromotion)
        createUpdateHeader = view.findViewById(R.id.tvCreateUpdateHeader)
        imageView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        buttonUpload.setOnClickListener {
            uploadImage()
        }

        // Setup Spinner
        val itemTypes = arrayOf("Drinks", "Sides", "Chicken")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        // Setup Slider
        sliderPromotion.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                editTextPromotion.isEnabled = progress > 0
                editTextPromotion.setText("$progress%")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Populate fields if editing existing menu item
        menuItem?.let {
            populateFields(it)
            buttonUpload.setText("Update Menu Item")
            createUpdateHeader.setText("Update Menu Item")
        }

        return view
    }

    private fun populateFields(menuItem: MenuItemDC) {
        Glide.with(requireContext())
            .load(menuItem.imageURL)
            .placeholder(R.drawable.cooked_chicken)
            .into(imageView)
        imageUri = Uri.parse(menuItem.imageURL)
        editTextName.setText(menuItem.itemName)
        editTextPrice.setText(String.format("%.2f",menuItem.itemPrice))
        editTextPromotion.setText(String.format("%.2f",menuItem.itemPromotion))
        spinnerType.setSelection(getIndex(spinnerType, menuItem.itemType))
        editTextPromotion.isEnabled = menuItem.itemPromotion > 0.0
        sliderPromotion.progress = (menuItem.itemPromotion*100).toInt()
    }

    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        if (::imageUri.isInitialized && imageUri.toString().isNotEmpty()) {
            if (imageUri.toString().startsWith("http")) {
                // Image URL already exists, use it directly
                saveImageUrlToFirestore(imageUri.toString())
            } else {
                // Upload new image
                val fileReference = storageReference.child("images/${System.currentTimeMillis()}.jpg")
                fileReference.putFile(imageUri).addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        saveImageUrlToFirestore(uri.toString())
                    }
                }
            }
        }
    }


    private fun saveImageUrlToFirestore(imageUrl: String) {
        val itemName = editTextName.text.toString()
        val itemPrice = String.format("%.2f", editTextPrice.text.toString().toDouble())
        val itemPromotion = if (editTextPromotion.isEnabled) editTextPromotion.text.toString().replace("%", "") else "0.00"
        val itemPromotionValue = (itemPromotion.toDouble() / 100).toString()
        val itemType = spinnerType.selectedItem.toString().lowercase()

        val item = hashMapOf(
            "imageUrl" to imageUrl,
            "itemName" to itemName,
            "itemPrice" to itemPrice,
            "itemPromotion" to itemPromotionValue,
            "itemType" to itemType
        )

        // Add or update menu item based on the presence of menuItem parameter
        if (menuItem == null) {
            firestore.collection("menuItem").add(item).addOnSuccessListener {
                //fragment.getMenuItems()
                resetForm()
                dismiss()
            }
        } else {
            // Update existing menu item based on item name
            val itemNameToUpdate = menuItem.itemName // Name of the item to update

            if (itemNameToUpdate != null) {
                // Find the document with the given item name
                firestore.collection("menuItem")
                    .whereEqualTo("itemName", itemNameToUpdate)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document =
                                documents.documents[0] // Assuming there's only one document with this name
                            document.reference.update(item as Map<String, Any>)
                            dismiss()
                        }
                    }
            }
        }
    }

    private fun resetForm() {
        imageView.setImageResource(R.drawable.image_upload)  // Replace with your placeholder image
        editTextName.text.clear()
        editTextPrice.text.clear()
        editTextPromotion.text.clear()
        editTextPromotion.isEnabled = false
        sliderPromotion.progress = 0
        spinnerType.setSelection(0)
        imageUri = Uri.EMPTY
    }
}



