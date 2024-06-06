package com.example.myapplication1.newfeatures.Chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication1.R
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.ActivityChatBinding
import com.example.myapplication1.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatFragment : Fragment() {

    private lateinit var adapter: ChatAdapter
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val messages = mutableListOf<MessageItem>()
    private var currentUser: UserItem? = null
    private lateinit var db: FirebaseDatabase
    private var chatRoomId = "app-publicChat"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getString("roomId")?.let {
                chatRoomId = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        db = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid

        // Configurar RecyclerView
        adapter =
            ChatAdapter(messages) // Se arregla dando a cada msj el id del usuario que lo mando
        binding.recyclerViewChat.adapter = adapter
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())

        // Manejar el clic en el botón de enviar
        binding.buttonSend.setOnClickListener {
            lifecycleScope.launch {
                val messageText = binding.editTextMessage.text.toString()
                if (messageText.isNotEmpty()) {
                    val message =
                        Message(messageText, userID.toString(), System.currentTimeMillis())
                    sendMessage(chatRoomId, message)
                    binding.editTextMessage.text.clear()
                }
            }
        }
        binding.buttonUsers.setOnClickListener {
            goToUsers()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenForMessages(chatRoomId) { message ->

        }
    }

    private suspend fun getUsersIds(): List<String> {
        db.getReference("Shop/Users").get().asDeferred().await().let { dataSnapshot ->
            val users = dataSnapshot.children.mapNotNull { it.getValue(UserItem::class.java) }
            currentUser = users.find { it.id == FirebaseAuth.getInstance().currentUser?.uid }
            adapter.notifyDataSetChanged()
            return users.map { it.id!! }
        }
    }

    suspend fun sendMessage(roomId: String, message: Message) {
        val existRoom = checkIfRoomExist(roomId)
        if (!existRoom) {
            // Si la sala de chat no existe, la creamos
            createChatRoom(roomId)
        }
        val roomRef = db.getReference("chatRooms/$roomId/messages")
        roomRef.push().setValue(message).asDeferred().await()
    }

    private suspend fun createChatRoom(roomId: String) {
        val chatRoom = ChatRoom(
            roomId = roomId,
            userIds = listOf(FirebaseAuth.getInstance().currentUser?.uid!!),
            messages = mapOf(UUID.randomUUID().toString() to Message())
        )
        db.getReference("chatRooms").child(chatRoom.roomId).setValue(chatRoom).asDeferred().await()
    }

    private suspend fun checkIfRoomExist(roomId: String): Boolean {
        return db.getReference("chatRooms/$roomId").get().asDeferred().await().exists()
    }

    fun listenForMessages(roomId: String, onNewMessage: (Message) -> Unit) {
        val messagesRef = db.getReference("chatRooms/$roomId/messages")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val newMessage = snapshot.getValue(Message::class.java)
                    if (newMessage != null && newMessage.text.isNotBlank()) {
                        lifecycleScope.launch {
                            messages.add(newMessage.toMessageItem())
                            adapter.notifyDataSetChanged() // Aquí se llama a notifyDataSetChanged()
                            _binding?.recyclerViewChat?.scrollToPosition(messages.size - 1)
                            onNewMessage(newMessage)
                            messages.removeIf { it.message.text.isBlank() }
                        }
                    }
                } catch (e: Exception) {
                    println("Error al escuchar mensajes: ${e.message}")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Se invoca cuando un mensaje existente cambia
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Se invoca cuando un mensaje es eliminado
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Se invoca cuando un mensaje cambia de posición
            }

            override fun onCancelled(error: DatabaseError) {
                // Se invoca cuando hay un error en la operación
                println("Error al escuchar mensajes: ${error.message}")
            }
        }

        messagesRef.addChildEventListener(childEventListener)
    }

    fun goToUsers() {
        try {
            findNavController().navigate(R.id.action_chatFragment_to_chatUsersFragment2)
        } catch (e: Exception) {
            Log.d("ChatFragment", "Error al navegar a chatUsersFragment2")
            findNavController().navigate(R.id.action_chatFragment2_to_chatUsersFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ChatFragment", "onDestroyView")
        _binding = null
    }
}