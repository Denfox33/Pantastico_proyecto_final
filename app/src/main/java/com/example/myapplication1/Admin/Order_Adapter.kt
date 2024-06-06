package com.example.myapplication1.Admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.myapplication1.R
import com.example.myapplication1.User.Product_Reservation
import com.example.myapplication1.databinding.ItemOrderBinding
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class Order_Adapter(val orders: MutableList<Product_Reservation>, val isAdmin: Boolean) :
    RecyclerView.Adapter<Order_Adapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: ItemOrderBinding) : RecyclerView.ViewHolder(itemView.root) {
        val name: TextView = itemView.tvItemCardName
        val type: TextView = itemView.tvItemCardCategory
        val price: TextView = itemView.tvItemCardPrice
        val status: TextView = itemView.tvItemCardAvailabilityData
        val image: ImageView = itemView.ivItemCardLogo
        val btnAccept: AppCompatButton = itemView.btnItemCardAceptOrder
        val btnCancel: AppCompatButton = itemView.btnItemCardCancel
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.name.text = order.productName
        holder.type.text = order.productType
        holder.price.text = order.price?.toString()
        holder.status.text = order.status
        Glide.with(holder.itemView.context).load(order.url_firebase_img).into(holder.image)

        // Hide buttons if user is not admin
        if (!isAdmin || order.status != "Pending") {
            holder.btnAccept.visibility = View.GONE
            holder.btnCancel.visibility = View.GONE
        } else {
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnCancel.visibility = View.VISIBLE
        }

        holder.btnAccept.setOnClickListener {
            updateOrderStatus(order, "Accepted")
        }

        holder.btnCancel.setOnClickListener {
            updateOrderStatus(order, "Cancelled")
        }
    }

    private fun updateOrderStatus(order: Product_Reservation, status: String) {
        getOrders(order){
        val db = FirebaseDatabase.getInstance().getReference()
        db.child("Shop").child("Pending_Orders/${it}/status").setValue(status)
            .addOnSuccessListener {
                orders.remove(order)
                notifyDataSetChanged()
            }
        }
    }

    fun getOrders(orderToFind: Product_Reservation,callback:(String) -> Unit): MutableList<Product_Reservation> {
        val db = FirebaseDatabase.getInstance().getReference()
        db.child("Shop").child("Pending_Orders").get().addOnSuccessListener {
            for (order in it.children) {
                val orderItem = order.getValue(Product_Reservation::class.java)
                if (orderItem?.id ==  orderToFind.id) {
                    callback(order.key.toString())
                }
            }
            notifyDataSetChanged()
        }
        return orders
    }
    override fun getItemCount() = orders.size
}