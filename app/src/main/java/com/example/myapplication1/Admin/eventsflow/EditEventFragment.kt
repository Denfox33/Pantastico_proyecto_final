package com.example.myapplication1.Admin.eventsflow

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentEditEventBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EditEventFragment : Fragment() {

    private var event: EventsItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           event = it.getParcelable("event")
        }
    }
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    val pickImageContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }
    private var imageUrl: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate binding
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.apply {
            editEventName.setText(event?.name)
            editEventMaxCapacity.setText(event?.maxcapacity!!.toString())
            editEventDate.setText(event?.date.toString())
            editEventPrice.setText(event?.price.toString())
            Glide.with(this@EditEventFragment).load(event?.url_firebase_logo).into(ivEventLogo)
            saveButton.setOnClickListener {
                lifecycleScope.launch {
                    editEvent()
                    findNavController().popBackStack()
                }
            }

            ivEventLogo.setOnClickListener {
                selectImage()
            }
        }

    }

    private suspend fun editEvent() {
        //Make me a function to edit the event
        if(imageUrl.isBlank()){
            imageUrl = event?.url_firebase_logo?:""
        }
        val eventUpdated = EventsItem(
            event?.id,
            binding.editEventName.text.toString(),
            binding.editEventMaxCapacity.text.toString(),
            binding.editEventDate.text.toString(),
            binding.editEventPrice.text.toString().toInt(),
            url_firebase_logo = imageUrl
        )
        update(eventUpdated)
    }

    private suspend fun update(eventUpdated: EventsItem) {
        val db = FirebaseDatabase.getInstance().getReference("Shop").child("Events")
        suspendCoroutine<Boolean> { continuation ->
            db.child(eventUpdated.id!!).setValue(eventUpdated).addOnSuccessListener {
                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show()
                continuation.resume(true)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                continuation.resume(false)
            }
        }

    }

    fun selectImage() {
        pickImageContract.launch("image/*")
    }

    fun uploadImage(uri: Uri) {
        //Make me a Function to upload image to the firebase storage
        val storageReference = FirebaseStorage.getInstance().getReference("Shop").child("Products")
        binding.saveButton.isEnabled = false
        val id = UUID.randomUUID().toString()
        val ref = storageReference.child(id)
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                binding.saveButton.isEnabled = true
                imageUrl = it.toString()
            }.addOnFailureListener { exception ->
                binding.saveButton.isEnabled = true
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}