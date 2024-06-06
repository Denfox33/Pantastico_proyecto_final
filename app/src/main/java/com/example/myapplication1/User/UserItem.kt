package com.example.myapplication1.User

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem (
    var id: String? = null,
    var user_type: String? = null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var date: String? = null,
    var url_firebase: String? = null,

    //saber si funciona bien y tener control para el estado
    var noti_status: Int? = null,
    var user_notification:String? = null

): Parcelable