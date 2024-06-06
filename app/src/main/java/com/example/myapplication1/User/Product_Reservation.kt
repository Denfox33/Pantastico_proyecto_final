package com.example.myapplication1.User

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Product_Reservation(
    val id: String? = null,
    val productId: String? = null,
    val userId: String? = null,
    val productName: String? = null,
    val productType: String? = null,
    val price: Double? = null,
    var status: String? = null,
    val date: String? = null,
    var url_firebase_img: String? = null,
    var noti_status: Int? = null,
    var user_notification: String? = null,
    var valoracionMedia: Double? = null
) : Parcelable