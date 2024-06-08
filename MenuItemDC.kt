package com.example.chucksgourmet

import android.os.Parcel
import android.os.Parcelable

data class MenuItemDC(var itemType:String, var itemName:String, var itemPrice:Double, var imageURL:String, var itemPromotion:Double):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemType)
        parcel.writeString(itemName)
        parcel.writeDouble(itemPrice)
        parcel.writeString(imageURL)
        parcel.writeDouble(itemPromotion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuItemDC> {
        override fun createFromParcel(parcel: Parcel): MenuItemDC {
            return MenuItemDC(parcel)
        }

        override fun newArray(size: Int): Array<MenuItemDC?> {
            return arrayOfNulls(size)
        }
    }
}