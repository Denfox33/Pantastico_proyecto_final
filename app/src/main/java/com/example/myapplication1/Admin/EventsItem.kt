package com.example.myapplication1.Admin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventsItem (
    var id: String? = null,
    var name: String? = null,
    var date: String? = null,
    var price: String? = null,
    var maxcapacity: Int? = null,
    var capacity: Int? = null,
    var url_firebase_logo: String? = null,



    //saber si funciona bien y tener control para el estado
    var noti_status: Int? = null,
    var user_notification:String? = null,

): Parcelable