package com.example.myapplication1.Admin.eventsflow

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.Admin.EventsAdapter
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.R
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.FragmentEventCheckDeleteEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EventFragmentCheckDeleteEdit : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var list: MutableList<EventsItem>
    private lateinit var db_ref: DatabaseReference
    private lateinit var adapter: EventsAdapter

    private var _binding: FragmentEventCheckDeleteEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventCheckDeleteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            list = mutableListOf()
            db_ref = FirebaseDatabase.getInstance().getReference()

            db_ref.child("Shop").child("Events")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        list.clear()
                        snapshot.children.forEach { child: DataSnapshot? ->
                            val event = child?.getValue(EventsItem::class.java)
                            list.add(event!!)
                        }
                        recyclerView.adapter?.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })

            recyclerView = binding.checkEventsList
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
            val isAdmin = requireContext().getSharedPreferences("MyApp", 0).getBoolean("isAdmin", false)
            adapter = EventsAdapter(list,isAdmin) {
                if (isAdmin) {
                    findNavController().navigate(
                        R.id.action_eventsFragment_to_editEventFragment,
                        Bundle().apply {
                            putParcelable("event", it)
                        })
                } else {
                    binding.mainAdd.visibility = View.GONE
                }
            }
            recyclerView.adapter = adapter
            // Configurar el ItemTouchHelper después de inicializar el adaptador y el RecyclerView
            val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT  // Permitir deslizar a la izquierda
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // No necesitas implementar el movimiento, devolver false
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Llama al método onItemDismiss del adaptador cuando se desliza un elemento
                    (recyclerView.adapter as EventsAdapter).onItemDismiss(viewHolder.adapterPosition)
                }
            }

            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(recyclerView)
            if (!isAdmin){
                binding.mainAdd.visibility = View.GONE
            }
            binding.mainAdd.setOnClickListener {
                findNavController().navigate(R.id.action_eventsFragmentCheckDeleteEdit_to_eventsFragmentAdd)
            }
            val searchUserInput = binding.seachUsernameInput
            val searchUserButton = binding.searchUserBtn

            searchUserButton.setOnClickListener {
                if (adapter != null) {
                    adapter.filter.filter(searchUserInput.text)
                }
            }

            searchUserInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (adapter != null) {
                        adapter.filter.filter(s)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("EventFragmentCheckDeleteEdit", "onDestroyView called")
        _binding = null
    }
}