package com.example.myapplication1.newfeatures.Chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.R
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.FragmentChatUsersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.asDeferred
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatUsersFragment : Fragment() {

    private var _binding: FragmentChatUsersBinding? = null
    private val binding get() = _binding!!
    private val newMessageUserIds = mutableListOf<String>()

    private lateinit var adapter: ChatUserAdapter  // Define adapter here


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatUsersBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonChatForo.setOnClickListener {
            findNavController().navigate(
                R.id.chatFragment,
                Bundle().apply {
                    putString("roomId", "app-publicChat")
                }
            )
        }
    }

    private fun initViews() {
        lifecycleScope.launch {
            val users = getUsers()
            val adapter = ChatUserAdapter(users, newMessageUserIds) { userClicked ->
                lifecycleScope.launch {
                    val user1Id =
                        FirebaseAuth.getInstance().currentUser?.uid // replace this with the id of the current user
                    val user2Id = userClicked.id
                    try {
                        val roomId = getPrivateChatRoom(user1Id = user1Id!!, user2Id = user2Id!!)
                        findNavController().navigate(
                            R.id.chatFragment,
                            Bundle().apply {
                                putString("roomId", roomId)
                            })
                    } catch (e: Exception) {
                        Log.d("ChatUsersFragment", "Error: ${e.message}")
                    }
                }
            }
            binding.recyclerViewChatUsers.adapter = adapter
            binding.recyclerViewChatUsers.layoutManager = LinearLayoutManager(requireContext())
            binding.seachUsernameInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // No es necesario implementar este método
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // No es necesario implementar este método
                }

                override fun afterTextChanged(s: Editable) {
                    val searchText = s.toString()
                    val filteredUsers = users.filter { user ->
                        user.name?.contains(searchText, ignoreCase = true) ?: false
                    }
                    (binding.recyclerViewChatUsers.adapter as ChatUserAdapter).updateUsers(filteredUsers)
                }
            })
        }
    }
    fun listenForMessages(roomId: String, onNewMessage: (Message) -> Unit) {
        val db = FirebaseDatabase.getInstance()
        val messagesRef = db.getReference("chatRooms/$roomId/messages")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val newMessage = snapshot.getValue(Message::class.java)
                    if (newMessage != null && newMessage.text.isNotBlank()) {
                        lifecycleScope.launch {
                            if (!newMessageUserIds.contains(newMessage.userId)) {
                                newMessageUserIds.add(newMessage.userId)
                                adapter.notifyDataSetChanged()
                            }
                            onNewMessage(newMessage)
                        }
                    }
                } catch (e: Exception) {
                    println("Error al escuchar mensajes: ${e.message}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Implement your logic here when a child item has changed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            // Implement other required methods...
        }
        messagesRef.addChildEventListener(childEventListener)
    }



    private suspend fun getPrivateChatRoom(
        user1Id: String,
        user2Id: String
    ): String {
        // Checkear si ya existe un chat room privado entre los dos usuarios
        var roomFind: ChatRoom? = null
        if (user2Id == "app-publicChat") {
            return "app-publicChat"
        }
        val db = FirebaseDatabase.getInstance()
        val chatRoomsRef = db.getReference("chatRooms")
        val rooms = chatRoomsRef
            .get().asDeferred().await()
        if (rooms.exists()) {
            roomFind = rooms.children.mapNotNull { snap ->
                try {
                    snap.getValue(ChatRoom::class.java)
                } catch (e: Exception) {
                    // Manejar errores de deserialización
                    println("Error al convertir a ChatRoom: ${e.message}")
                    null
                }
            }.find { privateRoom ->
                (privateRoom.userIds.contains(user1Id) && privateRoom.userIds.contains(user2Id)) || (privateRoom.userIds.contains(user2Id) && privateRoom.userIds.contains(user1Id)) && privateRoom.roomId != "app-publicChat"
            }
            if (roomFind != null) {
                return roomFind.roomId
            } else {
                val chatRoom = ChatRoom(
                    roomId = UUID.randomUUID().toString(),
                    userIds = listOf(user1Id, user2Id),
                    messages = mapOf(UUID.randomUUID().toString() to Message())
                )
                db.getReference("chatRooms").child(chatRoom.roomId).setValue(chatRoom)
                return chatRoom.roomId
            }
        }
        return ""
    }
    class ChatUserAdapter(
        private var users: List<UserItem>,
        private val newMessageUserIds: MutableList<String>,
        private val onUserClicked: (UserItem) -> Unit
    ) : RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder>() {

        // ...


        fun updateUsers(newUsers: List<UserItem>) {
            users = newUsers
            notifyDataSetChanged()
        }

        // ...


        inner class ChatUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val usernameTextView: TextView = itemView.findViewById(R.id.textViewUsername)
        }

        // ...

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
            return ChatUserViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {
            val user = users[position]
            holder.usernameTextView.text = user.name

            // Handle user click
            holder.itemView.setOnClickListener {
                onUserClicked(user)
            }
        }

        // ...



        override fun getItemCount(): Int {
            return users.size
        }

        // ...
    }

    private suspend fun getUsers(): List<UserItem> {
        val db = FirebaseDatabase.getInstance().reference
        val userlist = mutableListOf<UserItem>()

        return suspendCoroutine<List<UserItem>> { continuation ->
            db.child("Shop")
                .child("Users")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { hijo: DataSnapshot ->
                            val pojo_userItem = hijo.getValue(UserItem::class.java)
                            // Agregamos al usuario sin verificar si ha escrito en el chat público
                            lifecycleScope.launch {
                                if (pojo_userItem != null) {
                                    userlist.add(pojo_userItem)
                                }
                            }
                        }
                        continuation.resume(userlist)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                        continuation.resume(userlist)
                    }
                })
        }
    }
}