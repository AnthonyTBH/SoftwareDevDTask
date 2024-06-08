package com.example.chucksgourmet

import NotificationDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MenuItemRVAdapter(private val activity: AppCompatActivity, private val menuItemList:ArrayList<MenuItemDC>,private val cartViewModel: CartViewModel,private val isAdmin:Boolean?=null,private val fragmentManager: FragmentManager,private val context: Context): RecyclerView.Adapter<MenuItemRVAdapter.MenuItemViewHolder>() {
    class MenuItemViewHolder(menuItemView: View): RecyclerView.ViewHolder(menuItemView){
        val menuItemImg: ImageView = itemView.findViewById(R.id.imgMenu)
        val menuItemName: TextView = itemView.findViewById(R.id.tvMenuName)
        val menuItemPrice: TextView = itemView.findViewById(R.id.tvMenuPrice)
        val btnMenuItemAddToCart: Button = itemView.findViewById(R.id.btnMenuItemAddToCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_menu_template,parent,false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        Glide.with(activity)
            .load(menuItemList[position].imageURL)
            .placeholder(R.drawable.cooked_chicken)
            .into(holder.menuItemImg)
        holder.menuItemName.setText(menuItemList[position].itemName)

        holder.menuItemPrice.setText("Price: "+String.format("%.2f", menuItemList[position].itemPrice-(menuItemList[position].itemPrice*menuItemList[position].itemPromotion)))
        Log.d("MYTAG","in MenuItemRV, "+isAdmin.toString())
        if(isAdmin==null) {
            holder.btnMenuItemAddToCart.visibility = View.VISIBLE
            holder.btnMenuItemAddToCart.setOnClickListener{
                cartViewModel.addItem(menuItemList[position],null,1)
                val notificationDialog = NotificationDialog(activity,"Order added to cart!")
                notificationDialog.show()
            }
            holder.itemView.setOnClickListener {
                showDialog(menuItemList[position])
            }
        }
        else{
            holder.itemView.setOnLongClickListener {
                showAlertDialogWithActions(context, "Delete Menu Item", "Confirm delete menu item \"${menuItemList[position].itemName}\"?") {
                    val db = FirebaseFirestore.getInstance()

                    db.collection("menuItem")
                        .whereEqualTo("itemName", menuItemList[position].itemName)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("menuItem").document(document.id).delete()
                            }
                        }
                }
                true
            }
            holder.btnMenuItemAddToCart.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener {
                val createMenuFragment = CreateMenuFragment(menuItemList[position])
                createMenuFragment.show(fragmentManager, "CreateMenuFragment")
            }
        }
    }
    fun showAlertDialogWithActions(context: Context, title: String, message: String, onYesClicked: () -> Unit) {
        // Create an AlertDialog.Builder instance
        val builder = AlertDialog.Builder(context)

        // Set the dialog title and message
        builder.setTitle(title)
            .setMessage(message)

        // Add a "Yes" button
        builder.setPositiveButton("Yes") { dialog, _ ->
            // Execute the action when the "Yes" button is clicked
            onYesClicked.invoke()
            // Dismiss the dialog
            dialog.dismiss()
        }

        // Add a "No" button
        builder.setNegativeButton("No") { dialog, _ ->
            // Dismiss the dialog when the "No" button is clicked
            dialog.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun showDialog(menuItem:MenuItemDC) {
        val bottomSheetDialogFragment = if(menuItem.itemType=="chicken"){
            MenuBottomSheetDialogFragment()
        } else {
            SmallMenuBottomSheetDialogFragment()
        }
        val bundle = Bundle()
        bundle.putParcelable("menuItem", menuItem)
        bottomSheetDialogFragment.arguments = bundle
        bottomSheetDialogFragment.show(activity.supportFragmentManager, bottomSheetDialogFragment.tag)
    }

    override fun getItemCount(): Int {
        return menuItemList.size
    }
}