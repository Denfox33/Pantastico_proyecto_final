package com.example.myapplication1.Main


import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.R
import com.example.myapplication1.User.UserItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference

import kotlinx.coroutines.tasks.await

class Utilities {

    companion object{

        //==================================================================================================
        //==================================================================================================
        //==================================================================================================
        //USER FUNCTIONS
        fun user_Exist(userItems: List<UserItem>, name: String): Boolean {
            return userItems.any { it.name!!.lowercase() == name.lowercase() }
        }

        fun getUserList(db_ref: DatabaseReference): MutableList<UserItem> {
            var userlist = mutableListOf<UserItem>()

            db_ref.child("Shop")
                .child("Users")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { hijo: DataSnapshot ->
                            val pojo_userItem = hijo.getValue(UserItem::class.java)
                            userlist.add(pojo_userItem!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })

            return userlist
        }



        fun user_add(
            db_ref: DatabaseReference,
            id: String,
            user_type: String,
            name: String,
            email: String,
            password: String,
            date: String,
            url_firebase: String?,
            noti_status: Int,
            user_notification: String
        ) =
            db_ref.child("Shop").child("Users").child(id).setValue(
                UserItem(
                    id,
                    user_type,
                    name,
                    email,
                    password,
                    date,
                    url_firebase,
                    noti_status,
                    user_notification
                )
            )

        //==================================================================================================
        //==================================================================================================
        //==================================================================================================
        //EVENT FUNCTIONS


        fun events_Exist(events: List<EventsItem>, name: String): Boolean {
            return events.any { it.name!!.lowercase() == name.lowercase() }
        }
        fun getEventsList(db_ref: DatabaseReference): MutableList<EventsItem> {
            var eventlist = mutableListOf<EventsItem>()

            db_ref.child("Shop")
                .child("Events")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { hijo: DataSnapshot ->
                            val pojo_event = hijo.getValue(EventsItem::class.java)
                            eventlist.add(pojo_event!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })

            return eventlist
        }


        fun event_add(
            db_ref: DatabaseReference,
            id: String,
            name: String,
            date: String,
            price: String,
            maxCapacity: Int,
            Capacity: Int,
            url_firebase_logo: String,
            noti_status: Int,
            user_notification: String
        ) =
            db_ref.child("Shop").child("Events").child(id).setValue(
                EventsItem(
                    id,
                    name,
                    date,
                    price,
                    maxCapacity,
                    Capacity,
                    url_firebase_logo,
                    noti_status,
                    user_notification
                )
            )








        //==================================================================================================
        //==================================================================================================
        //==================================================================================================
        //PRODUCTS FUNCTIONS



                fun products_Exist(products: List<Producto_Item>, nombre: String): Boolean {
                    return products.any { it.nombre!!.lowercase() == nombre.lowercase() }
                }

                fun getProductsList(db_ref: DatabaseReference): MutableList<Producto_Item> {
                    var productList = mutableListOf<Producto_Item>()

                    db_ref.child("Shop")
                        .child("Products")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.children.forEach { hijo: DataSnapshot ->
                                    val pojo_product = hijo.getValue(Producto_Item::class.java)
                                    productList.add(pojo_product!!)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                println(error.message)
                            }
                        })

                    return productList
                }

                fun product_add(
                    db_ref: DatabaseReference,
                    id: String,
                    nombre: String,
                    tipo: String,
                    precio: Double,
                    stock: Int,
                    disponible: Boolean,
                    url_firebase_img: String,
                    valoracionMedia: Double, // Agregado
                    noti_status: Int,
                    user_notificacion: String
                ) =
                    db_ref.child("Shop").child("Products").child(id).setValue(
                        Producto_Item(
                            id,
                            nombre,
                            tipo,
                            precio,
                            stock,
                            disponible,
                            url_firebase_img,
                            valoracionMedia, // Agregado
                            noti_status,
                            user_notificacion
                        )
                    )



        //==================================================================================================
        //==================================================================================================
        //==================================================================================================
        //GLOBAL FUNCTIONS

        suspend fun save_logo(sto_ref: StorageReference, id: String, logo: Uri): String {
            lateinit var url_logo_firebase: Uri

            url_logo_firebase = sto_ref.child("Shop").child("ProfilePic").child(id)
                .putFile(logo).await().storage.downloadUrl.await()

            return url_logo_firebase.toString()
        }
        fun courrutine_thing_fragments(fragment: Fragment, context: Context, text: String) {
            fragment.requireActivity().runOnUiThread {
                Toast.makeText(
                    context,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun courrutine_thing(activity: AppCompatActivity, context: Context, text: String) {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun loading_animation(contexto: Context): CircularProgressDrawable {
            val animacion = CircularProgressDrawable(contexto)
            animacion.strokeWidth = 5f
            animacion.centerRadius = 30f
            animacion.start()
            return animacion
        }


        val transitions = DrawableTransitionOptions.withCrossFade(500)

        fun glideOptions(context: Context): RequestOptions {
            val options = RequestOptions()
                .placeholder(loading_animation(context))
                .fallback(R.drawable.baseline_person_24)
                .error(R.drawable.baseline_error_24)
            return options
        }

        fun getCurrentDate(): String {
            val date = java.util.Calendar.getInstance().time
            return date.toString()
        }

    }
}