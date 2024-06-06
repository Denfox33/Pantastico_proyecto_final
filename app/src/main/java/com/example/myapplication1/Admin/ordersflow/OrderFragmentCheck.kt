package com.example.myapplication1.Admin.ordersflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.Admin.Order_Adapter
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.User.Product_Reservation
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.FragmentOrderCheckBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OrderFragmentCheck : Fragment() {

    private var _binding: FragmentOrderCheckBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: Order_Adapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        database = FirebaseDatabase.getInstance().getReference("Shop").child("Pending_Orders")
        _binding = FragmentOrderCheckBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        adapter = Order_Adapter(mutableListOf(), true)
        binding.checkCardList.adapter = adapter

        binding.btnShowPending.setOnClickListener {
            showOrders("Pending")
        }

        binding.btnShowAll.setOnClickListener {
            showOrders("All")
        }

        showOrders("Pending")
    }

    private fun showOrders(status: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Product_Reservation::class.java) }
                val filteredOrders = if (status == "All") orders else orders.filter { it.status == "Pending" }
                adapter.orders.clear()
                adapter.orders.addAll(filteredOrders.reversed())
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    private fun cancelOrder(order: Product_Reservation) {
        // Cambiar el estado del pedido a "Cancelled"
        order.status = "Cancelled"
        // Actualizar el pedido en la base de datos
        database.child(order.id!!).setValue(order)
        // Aumentar el stock del producto
        increaseProductStock(order)
    }

    private fun increaseProductStock(cancelledOrder: Product_Reservation) {
        val dbRef = FirebaseDatabase.getInstance().getReference()
        val productId = cancelledOrder.productId
        val productRef = dbRef.child("Shop").child("Products").child(productId!!)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(Producto_Item::class.java)
                if (product != null) {
                    product.stock = product.stock?.plus(1)
                    productRef.setValue(product)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateProductStock(acceptedOrder: Product_Reservation) {
        val dbRef = FirebaseDatabase.getInstance().getReference()
        val productId = acceptedOrder.productId
        val productRef = dbRef.child("Shop").child("Products").child(productId!!)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(Producto_Item::class.java)
                if (product != null) {
                    product.stock = product.stock?.minus(1)
                    productRef.setValue(product)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
    private fun acceptOrder(order: Product_Reservation) {
        // Cambiar el estado del pedido a "aceptado"
        order.status = "Accepted"
        // Actualizar el pedido en la base de datos
        database.child(order.id!!).setValue(order)
        // Actualizar el stock del producto
        updateProductStock(order)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}