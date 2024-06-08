package com.example.chucksgourmet

import android.os.Parcel
import android.os.Parcelable

data class MenuSetDC(
    var setName: String,
    var setPrice: Double,
    var quantity:Int,
    var menuItems: ArrayList<MenuItemDC>,
    var menuItemQuantities: ArrayList<Int>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readInt(),
        ArrayList<MenuItemDC>().apply {
            parcel.readList(this as List<*>, MenuItemDC::class.java.classLoader)
        },
        parcel.createIntArray()?.toList() as ArrayList<Int>  // Read the IntArray as ArrayList<Int>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(setName)
        parcel.writeDouble(setPrice)
        parcel.writeInt(quantity)
        parcel.writeList(menuItems as List<*>?)
        parcel.writeIntArray(menuItemQuantities.toIntArray())  // Write the ArrayList<Int> as IntArray
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuSetDC> {
        override fun createFromParcel(parcel: Parcel): MenuSetDC {
            return MenuSetDC(parcel)
        }

        override fun newArray(size: Int): Array<MenuSetDC?> {
            return arrayOfNulls(size)
        }
    }
}
