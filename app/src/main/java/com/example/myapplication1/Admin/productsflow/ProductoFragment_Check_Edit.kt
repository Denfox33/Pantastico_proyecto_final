package com.example.myapplication1.Admin.productsflow

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentProductoCheckEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.ThreadInitializer
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProductoFragment_Check_Edit : Fragment() {

    private lateinit var binding: FragmentProductoCheckEditBinding
    private lateinit var adapter: Producto_Adapter
    private lateinit var db_ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductoCheckEditBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        lifecycleScope.launch {
            getProducts()
        }
    }

    private fun initViews() {
        //init recyclerview

        adapter = Producto_Adapter(
            mutableListOf(),
            isAdmin = requireContext().getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                .getBoolean("isAdmin", false)
        )
        binding.checkProductoList.adapter = adapter
        binding.mainAdd.setOnClickListener {
            findNavController().navigate(R.id.action_productsEditFragment_to_productFragmentAdds)
        }
    }

    private suspend fun getProducts() {
        val productList = mutableListOf<Producto_Item>()
        db_ref = FirebaseDatabase.getInstance().getReference()
        db_ref.child("Shop").child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productList.clear()
                    snapshot.children.forEach { child: DataSnapshot? ->
                        val product = child?.getValue(Producto_Item::class.java)
                        productList.add(product!!)
                    }
                    binding.checkProductoList.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })
        showProducts(productList)
    }

    private fun showProducts(productList: MutableList<Producto_Item>) {
        binding.checkProductoList.layoutManager = LinearLayoutManager(context)
        adapter = Producto_Adapter(productList, isAdmin =true)
        binding.checkProductoList.adapter = adapter
    }


}