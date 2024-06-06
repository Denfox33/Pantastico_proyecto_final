package com.example.myapplication1.newfeatures.Chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication1.R
import com.example.myapplication1.User.UserItem
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: List<MessageItem>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    val currentUser = FirebaseAuth.getInstance().currentUser

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.text_message)
        val messageTime: TextView = itemView.findViewById(R.id.time_message)
        val userName: TextView = itemView.findViewById(R.id.user_name_chat)
        val userPic: ImageView = itemView.findViewById(R.id.profileImageChat)
        val container: LinearLayout = itemView.findViewById(R.id.chat_item_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.message.text
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = sdf.format(Date(message.message.timestamp))
        holder.messageTime.text = date
        holder.userName.text = message.user.name
        if(message.user.url_firebase != "null" && message.user.url_firebase?.isNotBlank() == true){
            Glide.with(holder.itemView).load(message.user.url_firebase).into(holder.userPic)
        }
        if (message.message.userId == currentUser?.uid) {
            // Este mensaje es del usuario actual, alínealo a la derecha
            holder.container.gravity = Gravity.END
        } else {
            // Este mensaje es de otro usuario, alínealo a la izquierda
            holder.container.gravity = Gravity.START
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}