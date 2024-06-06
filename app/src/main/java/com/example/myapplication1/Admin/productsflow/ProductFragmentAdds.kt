package com.example.myapplication1.Admin.productsflow

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.databinding.FragmentProductAddsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

class ProductFragmentAdds : Fragment() {

    private var _binding: FragmentProductAddsBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var SaveButton: Button
    val pickImageContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ///inflating the layout for this fragment with binding
        _binding = FragmentProductAddsBinding.inflate(inflater, container, false)
        initViews()
        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().getReference("Shop").child("Products")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initViews() {
        //Make me a Function to initialize the views
        binding.productAddButton.setOnClickListener {
            lifecycleScope.launch {
                addProduct()
                findNavController().popBackStack()
            }
        }
        binding.glProductAddLogo.setOnClickListener {
            selectImage()
        }

        // Agregar TextWatchers a los campos de texto
        binding.productAddName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkFields()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        binding.productAddCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkFields()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        binding.productAddPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkFields()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        binding.productAddStock.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                checkFields()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    suspend fun addProduct() {
        // Asumiendo que tienes variables para imagen, nombre, categoria, precio y stock
        val image = imageUri
        val name = binding.productAddName.text.toString()
        val category = binding.productAddCategory.text.toString()
        val price = binding.productAddPrice.text.toString()
        val stock = binding.productAddStock.text.toString()

        // Verificar que el precio y el stock sean números
        val priceDouble = price.toDoubleOrNull()
        val stockInt = stock.toIntOrNull()

        // Verificar que todos los campos están completos
        if (image != null && name.isNotEmpty() && category.isNotEmpty() && priceDouble != null && stockInt != null) {
            // Si todos los campos están completos y son válidos, procede con la creación del producto
            Utilities.product_add(
                id = UUID.randomUUID().toString(),
                db_ref = databaseReference,
                nombre = name,
                precio = priceDouble,
                tipo = category,
                stock = stockInt,
                disponible = binding.productAddAvailability.isChecked,
                url_firebase_img = image.toString(),
                noti_status = 0,
                user_notificacion = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                valoracionMedia = 0.0
            ).asDeferred().await()

            // Navega a otra actividad
            findNavController().popBackStack()
        } else {
            // Si alguno de los campos no está completo o no es válido, muestra un mensaje de error
            Toast.makeText(
                context,
                "Por favor, complete todos los campos correctamente",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun checkFields() {
        // Asumiendo que tienes variables para imagen, nombre, categoria, precio y stock
        val image = imageUri
        val name = binding.productAddName.text.toString()
        val category = binding.productAddCategory.text.toString()
        val price = binding.productAddPrice.text.toString()
        val stock = binding.productAddStock.text.toString()

        // Verificar que el precio y el stock sean números
        val priceDouble = price.toDoubleOrNull()
        val stockInt = stock.toIntOrNull()

        // Verificar que todos los campos están completos// Si alguno de los campos no está completo o no es válido, deshabilita el botón de guardado
        // Si todos los campos están completos y son válidos, habilita el botón de guardado
        binding.productAddButton.isEnabled =
            image != null && name.isNotEmpty() && category.isNotEmpty() && priceDouble != null && stockInt != null
    }

    fun selectImage() {
        pickImageContract.launch("image/*")
    }

    fun uploadImage(uri: Uri) {
        //Make me a Function to upload image to the firebase storage
        val id = UUID.randomUUID().toString()
        val ref = storageReference.child(id)
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                imageUri = it
                // Actualizar la vista de la imagen
                binding.glProductAddLogo.setImageURI(it)
                // Llamar a checkFields() después de establecer imageUri
                checkFields()
            }
        }
        fun uploadImage(uri: Uri) {
            //Make me a Function to upload image to the firebase storage
            val id = UUID.randomUUID().toString()
            val ref = storageReference.child(id)
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    imageUri = it
                    // Usar Glide para cargar la imagen
                    Glide.with(this@ProductFragmentAdds)
                        .load(it)
                        .into(binding.glProductAddLogo)
                    // Llamar a checkFields() después de establecer imageUri
                    checkFields()
                }
            }
        }
    }
}