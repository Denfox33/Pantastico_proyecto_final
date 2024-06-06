package com.example.myapplication1.Admin.eventsflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.Admin.AssistAdapter
import com.example.myapplication1.Admin.AssistItem
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentAssistEventUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AssistEventsFragmentUser : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAssistEventUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AssistAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAssistEventUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn_events: AppCompatButton = binding.events


        database = FirebaseDatabase.getInstance().getReference("Shop").child("Pending_Events")
        val isAdmin = requireContext().getSharedPreferences("MyApp", 0).getBoolean("isAdmin", false)
        btn_events.setOnClickListener {
            if (isAdmin) {
                findNavController().navigate(R.id.eventsFragment)
            } else {
                findNavController().navigate(R.id.eventsFragment)
            }
        }

        binding.assistEventsFragmentUser.layoutManager = LinearLayoutManager(context)
        adapter = AssistAdapter(mutableListOf(), isAdmin)
        binding.assistEventsFragmentUser.adapter = adapter

        // Get current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        btn_events.setOnClickListener {
            findNavController().navigate(R.id.eventsFragmentUser)
        }
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val events = snapshot.children.mapNotNull { it.getValue(AssistItem::class.java) }
                // Filter events that belong to current user
                val currentUserEvents = if (isAdmin) {
                    events.filter { it.user_notification == currentUserId }
                } else {
                    events.filter { it.id_user == currentUserId }
                }

                adapter.items.clear()
                adapter.items.addAll(currentUserEvents.reversed())
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


}