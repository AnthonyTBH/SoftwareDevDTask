package com.example.chucksgourmet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class FeaturedMenuRVAdapter(private val activity: AppCompatActivity, private val menuList:ArrayList<MenuItemDC>, private val isAdmin:Boolean?=null, private val fragmentManager:FragmentManager?=null): RecyclerView.Adapter<FeaturedMenuRVAdapter.ViewHolder>(){
    private lateinit var context: Context
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val featuredMenuImg=itemView.findViewById<ImageView>(R.id.imgFeaturedMenu)
        val tvFeaturedMenuName = itemView.findViewById<TextView>(R.id.tvFeaturedMenuName)
        val tvFeaturedMenuOldPrice = itemView.findViewById<TextView>(R.id.tvFeaturedMenuOldPrice)
        val tvFeaturedMenuNewPrice = itemView.findViewById<TextView>(R.id.tvFeaturedMenuNewPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewype: Int): ViewHolder {
        context=parent.context
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rv_featured_menu_template,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(menuList[position].imageURL)
            .placeholder(R.drawable.cooked_chicken)
            .into(holder.featuredMenuImg)
        holder.tvFeaturedMenuName.setText(menuList[position].itemName)
        holder.tvFeaturedMenuOldPrice.setText("Price: "+String.format("%.2f", menuList[position].itemPrice))
        holder.tvFeaturedMenuNewPrice.setText("Price: "+String.format("%.2f", menuList[position].itemPrice-(menuList[position].itemPrice*menuList[position].itemPromotion)))
        if(isAdmin==null){
                holder.itemView.setOnClickListener {
                    showDialog(menuList[position])
                }
            }
        else{
            holder.itemView.setOnClickListener {
                val createMenuFragment = CreateMenuFragment(menuList[position],true)
                createMenuFragment.show(fragmentManager!!, "CreateMenuFragment")
            }
            holder.itemView.setOnLongClickListener {
                showAlertDialogWithActions(context, "Delete Menu Item", "Confirm delete menu item \"${menuList[position].itemName}\"?") {
                    val db = FirebaseFirestore.getInstance()

                    db.collection("menuItem")
                        .whereEqualTo("itemName", menuList[position].itemName)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                db.collection("menuItem").document(document.id).delete()
                            }
                        }
                }
                true
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
}