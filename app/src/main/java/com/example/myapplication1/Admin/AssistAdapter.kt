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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AssistAdapter(val items: MutableList<AssistItem>, val isAdmin: Boolean) :
    RecyclerView.Adapter<AssistAdapter.AssistViewHolder>() {

    class AssistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_item_event_name_data)
        val date: TextView = itemView.findViewById(R.id.tv_item_event_date_data)
        val price: TextView = itemView.findViewById(R.id.tv_item_event_price_data)
        val capacity: TextView = itemView.findViewById(R.id.tv_item_event_capacity_data)
        val maxcapacity: TextView = itemView.findViewById(R.id.tv_item_event_maxcapacity_data)
        val image: ImageView = itemView.findViewById(R.id.iv_item_event_logo)
        val btnCancel: AppCompatButton = itemView.findViewById(R.id.btn_item_event_cancel)
        val btnJoin: AppCompatButton = itemView.findViewById(R.id.btn_item_event_join)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return AssistViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssistViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.date.text = item.date
        holder.price.text = item.price
        holder.capacity.text = item.capacity?.toString()
        holder.maxcapacity.text = item.maxcapacity?.toString()
        Glide.with(holder.itemView.context).load(item.url_firebase_logo).into(holder.image)


        // Hide join button
        holder.btnJoin.visibility = View.GONE

        // Show cancel button if user is admin and if the event ID is in Pending_Events
        CoroutineScope(Dispatchers.Main).launch {
            val isPending = isPending(item.id!!)
            if (isPending) {
                holder.btnCancel.visibility = View.VISIBLE
            } else {
                holder.btnCancel.visibility = View.GONE
            }
        }


        holder.btnCancel.setOnClickListener {
            // Start a transaction on the event node
            val eventRef = FirebaseDatabase.getInstance().getReference("Shop").child("Events")
                .child(item.id_event!!)
            eventRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val event = mutableData.getValue(EventsItem::class.java)
                        ?: return Transaction.success(mutableData)

                    // Decrease the capacity by 1
                    event.capacity = (event.capacity ?: 0) - 1

                    // Set the updated event object to the mutableData
                    mutableData.value = event

                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    // Transaction completed
                    if (committed) {
                        // Remove the AssistItem from Pending_Events
                        val pendingEventRef = FirebaseDatabase.getInstance().getReference("Shop")
                            .child("Pending_Events").child(item.id!!)
                        pendingEventRef.removeValue().addOnSuccessListener {
                            items.remove(item)
                            notifyDataSetChanged()
                        }
                    }
                }
            })
        }
    }

    private suspend fun isPending(id: String): Boolean {
        var isPending = false
        val data =
            FirebaseDatabase.getInstance().reference.child("Shop").child("Pending_Events").get()
                .asDeferred().await()
        if (data.exists()) {
            for (child in data.children) {
                child.getValue(AssistItem::class.java)?.let { item ->
                    isPending =  item.id == id
                }
            }
        }
        return isPending
    }


    override fun getItemCount() = items.size
}