package com.example.myapplication1.newfeatures.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.UserItemBinding

class ChatUserAdapter(private val users: List<UserItem>, private val newMessageUserIds: List<String>, private val navigate:(UserItem) -> Unit) : RecyclerView.Adapter<ChatUserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        if (newMessageUserIds.contains(user.id)) {
            holder.binding.newMessageIndicator.visibility = View.VISIBLE
        } else {
            holder.binding.newMessageIndicator.visibility = View.GONE
        }
        holder.binding.textViewUsername.text = user.name
        Glide.with(holder.itemView.context).load(user.url_firebase).into(holder.binding.chatProfileImage)
        holder.binding.userProfile.setOnClickListener {
            navigate(user)
        }
    }

    override fun getItemCount() = users.size
}