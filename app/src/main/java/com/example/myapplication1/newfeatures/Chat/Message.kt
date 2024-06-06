package com.example.myapplication1.newfeatures.Chat

import com.example.myapplication1.User.UserItem
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.asDeferred
import java.util.UUID

data class Message(
    val text: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
    // val id: String = UUID.randomUUID().toString(),
) {
}

data class ChatRoom(
    val roomId: String = "",
    val userIds: List<String> = emptyList(),
    val messages: Map<String, Message> = emptyMap()
)

data class MessageItem(
    val message: Message,
    val user: UserItem,
    // val id: String = UUID.randomUUID().toString(),
)

suspend fun Message.toMessageItem(): MessageItem {
    val userSnap = FirebaseDatabase.getInstance().reference.child("Shop/Users").child(userId).get()
        .asDeferred().await()
    return userSnap.getValue(UserItem::class.java)?.let { user ->
        MessageItem(this, user)
    }
        ?: throw IllegalStateException("User not found")}