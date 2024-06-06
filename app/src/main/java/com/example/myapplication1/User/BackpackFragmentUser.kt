package com.example.myapplication1.User

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.Admin.Order_Adapter
import com.example.myapplication1.databinding.FragmentBackpackUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BackpackFragmentUser : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentBackpackUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: Order_Adapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBackpackUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("Shop").child("Pending_Orders")

        binding.bpFragmentUser.layoutManager = LinearLayoutManager(context)
        adapter = Order_Adapter(mutableListOf(), false)
        binding.bpFragmentUser.adapter = adapter

        // Get current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Product_Reservation::class.java) }
                // Filter cards with status "Accepted" and belong to current user
                val currentUserAcceptedOrders = orders.filter {  it.userId == currentUserId }
                adapter.orders.clear()
                adapter.orders.addAll(currentUserAcceptedOrders.reversed())
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BackpackFragmentUser().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}