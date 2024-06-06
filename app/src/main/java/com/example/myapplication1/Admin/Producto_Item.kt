package com.example.myapplication1.Admin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Producto_Item(
    var id: String? = null,
    var nombre: String? = null,
    var tipo: String? = null,
    var precio: Double? = null,
    var stock: Int? = null,
    var disponible: Boolean = true,
    var url_firebase_img: String? = null,
    var valoracionMedia: Double? = null,

    //saber si funciona bien y tener control para el estado
    var noti_status: Int? = null,
    var user_notificacion: String? = null
) : Parcelable