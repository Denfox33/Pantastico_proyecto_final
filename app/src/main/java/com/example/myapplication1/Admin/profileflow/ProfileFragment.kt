package com.example.myapplication1.Admin.profileflow

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.myapplication1.Admin.EventsItem
import com.example.myapplication1.Admin.Producto_Item
import com.example.myapplication1.Main.Login
import com.example.myapplication1.databinding.FragmentProfileBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createSalesChart()
        createEventChart()
        binding.buttonLogout.setOnClickListener {
            // Update authentication state in SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("MyApp",
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.apply()

            // Sign out from FirebaseAuth
            auth.signOut()
            Log.d("ProfileFragmentUser", "Usuario cerró sesión")

            // Redirect to Login activity
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            activity?.finish()
        }
        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Change to dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else {
                // Change to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun createSalesChart() {
        FirebaseDatabase.getInstance().getReference("Shop").child("Products").get().addOnCompleteListener {
            if (it.isSuccessful) {
                val products = it.result.children.mapNotNull { it.getValue(Producto_Item::class.java) }
                val entries: ArrayList<BarEntry> = ArrayList()
                for (i in products.indices) {
                    entries.add(BarEntry(i.toFloat(), products[i].precio?.toFloat() ?: 0.0f)) //Posible agregar otro campo que sea cantidad de ventas por producto
                }
                val barDataSet = BarDataSet(entries, "Products")
                barDataSet.color = Color.BLUE

                val dataSets: ArrayList<IBarDataSet> = ArrayList()
                dataSets.add(barDataSet)

                val data = BarData(dataSets)
                data.barWidth = 0.5f

                binding.barChart.data = data
                binding.barChart.setFitBars(true)
                binding.barChart.invalidate()
            } else {
                // Handle error
                Log.d("ProfileFragment", "Error getting data")
            }
        }

    }

    private fun createEventChart(){
        FirebaseDatabase.getInstance().getReference("Shop").child("Events").get().addOnCompleteListener {
            if (it.isSuccessful) {
                val events = it.result.children.mapNotNull { it.getValue(EventsItem::class.java) }
                val entries: ArrayList<BarEntry> = ArrayList()
                for (i in events.indices) {
                    entries.add(BarEntry(i.toFloat(), events[i].price?.toFloat() ?: 0.0f)) //Posible agregar otro campo que sea cantidad de gente que asistio al evento
                }
                val barDataSet = BarDataSet(entries, "Events")
                barDataSet.color = Color.RED

                val dataSets: ArrayList<IBarDataSet> = ArrayList()
                dataSets.add(barDataSet)

                val data = BarData(dataSets)
                data.barWidth = 0.5f

                binding.barChartEvent.data = data
                binding.barChartEvent.setFitBars(true)
                binding.barChartEvent.invalidate()
            } else {
                // Handle error
                Log.d("ProfileFragment", "Error getting data")
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}