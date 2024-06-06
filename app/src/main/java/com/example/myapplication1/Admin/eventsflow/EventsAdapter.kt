package com.example.myapplication1.Admin




import android.content.Context
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication1.Main.ItemTouchHelperAdapter
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.text.SimpleDateFormat
import java.util.Date

class EventsAdapter(private val events_list: MutableList<EventsItem>, private val isAdmin: Boolean,private val navigate: (EventsItem) -> Unit):
    RecyclerView.Adapter<EventsAdapter.EventsViewHolder>(), Filterable, ItemTouchHelperAdapter {

    private lateinit var context: Context
    private var filtered_list = events_list
    private val db = FirebaseDatabase.getInstance().getReference()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventsAdapter.EventsViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        context = parent.context
        return EventsViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: EventsAdapter.EventsViewHolder, position: Int) {
        val current_object = filtered_list[position]
        holder.name.text = current_object.name
        holder.date.text = current_object.date.toString()
        holder.price.text = current_object.price.toString()
        holder.maxcapacity.text = current_object.maxcapacity?.toString() ?: "0"
        holder.capacity.text = current_object.capacity?.toString() ?: "0"

        val URL: String? = when (current_object.url_firebase_logo) {
            "" -> null
            else -> current_object.url_firebase_logo
        }

        Glide.with(context)
            .load(URL)
            .apply(Utilities.glideOptions(context))
            .transition(Utilities.transitions)
            .into(holder.logo)

        // Check Firebase for existing reservations for the current event
        val reservationsRef = db.child("Shop").child("Pending_Events")
        reservationsRef.orderByChild("id_event").equalTo(current_object.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var reservationExists = false
                for (reservationSnapshot in snapshot.children) {
                    val reservation = reservationSnapshot.getValue(AssistItem::class.java)
                    if (reservation?.id_user == FirebaseAuth.getInstance().currentUser?.uid) {
                        reservationExists = true
                        break
                    }
                }
                if (reservationExists || current_object.maxcapacity == current_object.capacity) {
                    holder.btnJoin.visibility = View.GONE
                } else if(!isAdmin){
                    holder.btnJoin.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        holder.constraint_row.setOnClickListener {
            navigate(current_object)
        }
        if(isAdmin){
            holder.btnJoin.visibility = View.GONE
        }
        //Check for admin
        holder.btnJoin.setOnClickListener {
            val reservationId = db.child("Shop").child("Pending_Events").push().key
            val reservation = AssistItem(
                id = reservationId,
                name = current_object.name,
                date = current_object.date,
                price = current_object.price,
                maxcapacity = current_object.maxcapacity,
                capacity = current_object.capacity?.plus(1),
                url_firebase_logo = current_object.url_firebase_logo,
                id_event = current_object.id,
                id_user = FirebaseAuth.getInstance().currentUser?.uid,
                noti_status = 0, // You can set this value as per your requirement
                user_notification = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            )
            reservationId?.let {
                db.child("Shop").child("Pending_Events").child(it).setValue(reservation).addOnSuccessListener {
                    Toast.makeText(context, "Event joined successfully", Toast.LENGTH_LONG).show()
                    // Hide the join button after successfully joining the event
                    holder.btnJoin.visibility = View.GONE
                    // Update the capacity and maxcapacity values
                    current_object.capacity = current_object.capacity?.plus(1)
                    holder.capacity.text = current_object.capacity?.toString() ?: "0"
                    holder.maxcapacity.text = current_object.maxcapacity?.toString() ?: "0"
                    // Update the event in the Firebase database
                    db.child("Shop").child("Events").child(current_object.id!!).setValue(current_object)
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to join event", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = filtered_list.size

    class EventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.iv_item_event_logo)
        val name: TextView = itemView.findViewById(R.id.tv_item_event_name_data)
        val date: TextView = itemView.findViewById(R.id.tv_item_event_date_data)
        val price: TextView = itemView.findViewById(R.id.tv_item_event_price_data)
        val capacity: TextView = itemView.findViewById(R.id.tv_item_event_capacity_data)
        val maxcapacity: TextView = itemView.findViewById(R.id.tv_item_event_maxcapacity_data)
        val constraint_row: ConstraintLayout = itemView.findViewById(R.id.constraint_row)
        val btnJoin: AppCompatButton = itemView.findViewById(R.id.btn_item_event_join)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val busqueda = p0.toString().lowercase()
                if (busqueda.isEmpty()) {
                    filtered_list = events_list
                } else {
                    filtered_list = (events_list.filter {
                        it.name.toString().lowercase().contains(busqueda)
                    }) as MutableList<EventsItem>
                }

                val filterResults = FilterResults()
                filterResults.values = filtered_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }

    override fun onItemDismiss(position: Int) {
        synchronized(filtered_list) {
            if (position >= 0 && position < filtered_list.size) {
                // Obtener referencia a Firebase
                val dbRef = FirebaseDatabase.getInstance().getReference()
                val stoRef = FirebaseStorage.getInstance().getReference()
                val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

                // Get the Event object at the position
                val EventsAdmin = filtered_list[position]

                // Actualizar en Firebase y almacenamiento
                dbRef.child("Shop").child("Events").child(EventsAdmin.id!!)
                    .child("user_notification").setValue(androidId)

                stoRef.child("Shop").child("ProfilePic").child(EventsAdmin.id!!).delete()

                dbRef.child("Shop").child("Events").child(EventsAdmin.id!!).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Eliminar localmente solo si Firebase se actualiza correctamente
                            synchronized(filtered_list) {
                                if (position < filtered_list.size) {
                                    filtered_list.removeAt(position)
                                    notifyItemRemoved(position)
                                }
                            }
                            // Obtener la lista actualizada de eventos desde Firebase
                            dbRef.child("Shop").child("Events").addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    filtered_list.clear()
                                    snapshot.children.forEach { hijo: DataSnapshot ->
                                        val event = hijo.getValue(EventsAdmin::class.java)
                                        filtered_list.add(event!!)
                                    }
                                    notifyDataSetChanged() // Notificar al RecyclerView que los datos han cambiado
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    println(error.message)
                                }
                            })
                            Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            // Manejar el caso en que la eliminación en Firebase falla
                            Toast.makeText(context, "Error deleting Event", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}