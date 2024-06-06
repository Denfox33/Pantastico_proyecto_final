package com.example.myapplication1.Admin.productsflow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.Main.Utilities
import com.example.myapplication1.R
import com.example.myapplication1.User.Product_Reservation
import com.example.myapplication1.User.UserItem
import com.example.myapplication1.databinding.ItemProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.util.UUID

class Producto_Adapter(private val products_list: MutableList<Producto_Item>, private val isAdmin: Boolean) :
    RecyclerView.Adapter<Producto_Adapter.ProductoViewHolder>(), Filterable {

    private lateinit var context: Context
    private var filtered_list = products_list
    private val db = FirebaseDatabase.getInstance().getReference()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Producto_Adapter.ProductoViewHolder {
        //binding

        val item_view = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ProductoViewHolder(item_view)
    }

    fun updateProducts(newProductList: List<Producto_Item>) {
        products_list.clear()
        products_list.addAll(newProductList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: Producto_Adapter.ProductoViewHolder, position: Int) {
        val current_object = filtered_list[position]
        holder.name.text = current_object.nombre
        holder.category.text = current_object.tipo
        holder.price.text = current_object.precio.toString()
        holder.stock.text = current_object.stock?.toString() ?: "0"
        holder.availability.text = if(current_object.disponible == true) "Disponible" else "No disponible"
        holder.valoracionMedia.rating = current_object.valoracionMedia?.toFloat() ?: 0f // Agregado
        val URL: String? = when (current_object.url_firebase_img) {
            "" -> null
            else -> current_object.url_firebase_img
        }

        Glide.with(context)
            .load(URL)
            .apply(Utilities.glideOptions(context))
            .transition(Utilities.transitions)
            .into(holder.logo)

        // Aquí va el resto del código de onBindViewHolder, actualizado para Producto_Item
        if (isAdmin) {
            holder.btnBuy.visibility = View.GONE
            holder.constraint_row.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelable("product", current_object)
                }
                it.findNavController()
                    .navigate(R.id.action_productsEditFragment_to_editProductFragment, bundle)

            }
        }else{
            holder.btnBuy.setOnClickListener {
                if (current_object.stock == 0 || !current_object.disponible) {
                    Toast.makeText(context, "Este producto no está disponible actualmente.", Toast.LENGTH_SHORT).show()
                } else {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    val ref = db.child("Shop").child("Pending_Orders").push()
                    val order = Product_Reservation(
                        id = UUID.randomUUID().toString(),
                        userId = currentUserId,
                        productId = current_object.id,
                        productName = current_object.nombre,
                        price = current_object.precio,
                        url_firebase_img = current_object.url_firebase_img,
                        productType = current_object.tipo,
                        status = "Pending",
                        date = Utilities.getCurrentDate(),
                        noti_status = current_object.noti_status,
                        user_notification = current_object.user_notificacion,
                        valoracionMedia = current_object.valoracionMedia
                    )
                    ref.setValue(order)
                    updateProductStock(current_object)
                }
            }
        }
    }

    override fun getItemCount(): Int = filtered_list.size

    class ProductoViewHolder(itemView: ItemProductBinding) : RecyclerView.ViewHolder(itemView.root) {
        val logo: ImageView = itemView.ivItemProLogo
        val name: TextView = itemView.tvItemCardName
        val category: TextView = itemView.tvItemCardCategory
        val price: TextView = itemView.tvItemCardPrice
        val stock: TextView = itemView.tvItemCardStock
        val availability: TextView = itemView.tvItemCardAvailability
        val valoracionMedia: RatingBar = itemView.tvItemProductValoracionMedia // Corregido
        val constraint_row: ConstraintLayout = itemView.constraintRow
        val btnBuy: AppCompatButton = itemView.btnItemCardBuy
        val btnCancel: AppCompatButton = itemView.btnItemProCancel
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val busqueda = p0.toString().lowercase()
                if (busqueda.isEmpty()) {
                    filtered_list = products_list
                } else {
                    val listaFiltrada = ArrayList<Producto_Item>()
                    for (producto in products_list) {
                        if (producto.nombre?.lowercase()?.contains(busqueda) == true) {
                            listaFiltrada.add(producto)
                        }
                    }
                    filtered_list = listaFiltrada
                }

                val filterResults = FilterResults()
                filterResults.values = filtered_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                filtered_list = p1?.values as ArrayList<Producto_Item>
                notifyDataSetChanged()
            }
        }
    }

    private fun updateProductStock(product: Producto_Item) {
        val dbRef = FirebaseDatabase.getInstance().getReference()
        val productId = product.id
        val productRef = dbRef.child("Shop").child("Products").child(productId!!)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(Producto_Item::class.java)
                if (product != null) {
                    product.stock = product.stock?.minus(1)
                    productRef.setValue(product)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun increaseProductStock(product: Producto_Item) {
        val dbRef = FirebaseDatabase.getInstance().getReference()
        val productId = product.id
        val productRef = dbRef.child("Shop").child("Products").child(productId!!)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val product = dataSnapshot.getValue(Producto_Item::class.java)
                if (product != null) {
                    product.stock = product.stock?.plus(1)
                    productRef.setValue(product)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    class SliderAdapter(private val context: Context, private val images: List<Int>) :
        RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

        inner class SliderViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
            val imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            return SliderViewHolder(imageView)
        }

        override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
            Glide.with(context)
                .load(images[position])
                .into(holder.imageView)
        }

        override fun getItemCount(): Int = images.size
    }
}