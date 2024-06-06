package com.example.myapplication1.Admin.productsflow

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentEditProductBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine



class EditProductFragment : Fragment() {

    private lateinit var product: Producto_Item
    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    val pickImageContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImage(it)
            }
        }
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getParcelable("product") ?: throw Exception("Product not found")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProductBinding.inflate(layoutInflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.apply {
            editProductName.setText(product.nombre)
            editProductType.setText(product.tipo)
            editProductPrice.setText(product.precio.toString())
            editProductStock.setText(product.stock.toString())
            editProductAvailable.isChecked = product.disponible
            if (product.url_firebase_img?.isNotBlank() == true && product.url_firebase_img != "null"){
                Glide.with(this@EditProductFragment).load(product.url_firebase_img)
                    .into(editIvItemProductLogo).onLoadFailed(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.portada
                        )
                    )
            }
            saveButton.setOnClickListener {
                lifecycleScope.launch {
                    editProduct()
                    findNavController().popBackStack()
                }
            }
            deleteButton.setOnClickListener {
                lifecycleScope.launch {
                    deleteProduct()
                    findNavController().popBackStack()
                }
            }
            editIvItemProductLogo.setOnClickListener {
                selectImage()
            }
        }
    }

    suspend fun editProduct() {
        binding.apply {
            val name = editProductName.text.toString()
            val type = editProductType.text.toString()
            val price = editProductPrice.text.toString().toDouble()
            val stock = editProductStock.text.toString().toInt()
            val available = editProductAvailable.isChecked
            if (name.isBlank() || type.isBlank() || price.isNaN() || stock < 0) {
                Toast.makeText(context, "Please fill all the fields correctly", Toast.LENGTH_SHORT)
                    .show()
                return
            }

            if (imageUrl.isBlank()) {
                imageUrl = product.url_firebase_img ?: ""
            }
            val newProduct = Producto_Item(
                product.id,
                name,
                type,
                price,
                stock,
                available,
                imageUrl,
                product.valoracionMedia
            )
            // Update product in firebase real time database
            withContext(Dispatchers.IO) {
                updateProduct(newProduct)
            }
        }

    }

    private suspend fun updateProduct(updatedProduct: Producto_Item) {
        val db = FirebaseDatabase.getInstance().getReference("Shop/Products/${updatedProduct.id}")
        suspendCoroutine<Boolean> { continuation ->
            db.setValue(updatedProduct).addOnSuccessListener {
                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show()
                continuation.resume(true)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show()
                continuation.resume(false)
            }
        }

    }

    private suspend fun deleteProduct() {
        val db = FirebaseDatabase.getInstance().getReference("Shop/Products/${product.id}")
        suspendCoroutine<Boolean> { continuation ->
            db.removeValue().addOnSuccessListener {
                Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                continuation.resume(true)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show()
                continuation.resume(false)
            }
        }
    }

    fun selectImage() {
        pickImageContract.launch("image/*")
    }

    fun uploadImage(uri: Uri) {
        //Make me a Function to upload image to the firebase storage
        binding.saveButton.isEnabled = false
        val storageReference = FirebaseStorage.getInstance().getReference("Shop").child("Products")
        val id = UUID.randomUUID().toString()
        val ref = storageReference.child(id)
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                imageUrl = it.toString()
                binding.saveButton.isEnabled = true
            }.addOnFailureListener {
                binding.saveButton.isEnabled = true
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


