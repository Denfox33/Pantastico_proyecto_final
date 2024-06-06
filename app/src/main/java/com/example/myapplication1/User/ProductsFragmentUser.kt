package com.example.myapplication1.User

import android.widget.TextView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication1.Admin.productsflow.Producto_Adapter
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.R
import com.example.myapplication1.databinding.FragmentProductsUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ProductsFragmentUser : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var list: MutableList<Producto_Item>
    private lateinit var db_ref: DatabaseReference
    private lateinit var adapter: Producto_Adapter
    private var isSortedAscending = true
    private var _binding: FragmentProductsUserBinding? = null
    private val binding get() = _binding!!

    // carrusel
    private lateinit var viewPager: ViewPager2
    private lateinit var sliderAdapter: Producto_Adapter.SliderAdapter

    //tiempo
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductsUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerCurrency = view.findViewById<Spinner>(R.id.spinner_currency)
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val currency = parent.getItemAtPosition(position).toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    updatePrices(currency)
                }
                updateCurrencyText(currency)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        list = mutableListOf()
        db_ref = FirebaseDatabase.getInstance().getReference()

        val searchUserInput = binding.seachUsernameInput
        val searchUserButton = binding.searchUserBtn

        searchUserButton.setOnClickListener {
            if (adapter != null) {
                adapter.filter.filter(searchUserInput.text)
            }
        }

        searchUserInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (adapter != null) {
                    adapter.filter.filter(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSortPrice.setOnClickListener {
            list.sortBy { it.precio }
            adapter.notifyDataSetChanged()
        }

        binding.btnUpPrice.setOnClickListener {
            list.sortByDescending { it.precio }
            adapter.notifyDataSetChanged()
        }

        db_ref.child("Shop").child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    snapshot.children.forEach { child: DataSnapshot? ->
                        val product = child?.getValue(Producto_Item::class.java)
                        product?.let { list.add(it) }
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        recyclerView = binding.checkProductoList
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        adapter = Producto_Adapter(list, false)
        recyclerView.adapter = adapter

        // Inicializa el ViewPager2 y el adaptador del carrusel
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val images = listOf(
            R.drawable.choco,
            R.drawable.galleta,
            R.drawable.manzana,
            R.drawable.pastelito,
            R.drawable.surtido,
            R.drawable.tarta,
            // Agrega más recursos de imagen según sea necesario
        )
        val sliderAdapter = Producto_Adapter.SliderAdapter(requireContext(), images)
        viewPager.adapter = sliderAdapter

        runnable = Runnable {
            viewPager.currentItem = (viewPager.currentItem + 1) % images.size
            handler.postDelayed(runnable, 3000)
        }
        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        _binding = null
    }

    private suspend fun updatePrices(currency: String) {
        val rate = when (currency) {
            "€ Euro" -> getCurrencyRate("EUR")
            "$ Dólar" -> getCurrencyRate("USD")
            "¥ Yen" -> getCurrencyRate("JPY")
            else -> 1.0
        }

        list.forEach { product ->
            product.precio = product.precio?.let { convertPrice(it, rate) }
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateCurrencyText(currency: String) {
        val currencySymbol = when (currency) {
            "€ Euro" -> "€"
            "$ Dólar" -> "$"
            "¥ Yen" -> "¥"
            else -> ""
        }

        val textViewCurrency = view?.findViewById<TextView>(R.id.moneda)
        if (textViewCurrency != null) {
            textViewCurrency.text = currencySymbol
        }
    }

    // Aquí debes implementar las funciones getCurrencyRate y convertPrice
    private fun convertPrice(price: Double, rate: Double): Double {
        val result = price * rate
        return String.format("%.2f", result).toDouble()
    }

    // Estas funciones deben obtener la tasa de cambio actual y convertir los precios de los productos a la moneda seleccionada
    private var currentCurrency =
        "EUR" // Moneda actual del precio. Puedes cambiar esto según sea necesario.

    private suspend fun getCurrencyRate(targetCurrency: String): Double {
        return when (currentCurrency) {
            "EUR" -> when (targetCurrency) {
                "USD" -> 1 / 0.85
                "JPY" -> 110.0
                "EUR" -> 0.85

                else -> 1.0
            }

            "USD" -> when (targetCurrency) {
                "EUR" -> 0.85
                "JPY" -> 110.0 * 0.85
                "USD" -> 1 / 0.85
                else -> 1.0
            }

            "JPY" -> when (targetCurrency) {
                "EUR" -> 0.0076
                "USD" -> 0.0089
                "JPY" -> 1.0
                else -> 1.0
            }

            else -> 1.0
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
}