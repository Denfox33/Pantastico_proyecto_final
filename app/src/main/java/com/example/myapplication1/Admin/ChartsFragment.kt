package com.example.myapplication1.Admin

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myapplication1.databinding.FragmentChartsBinding
import com.example.myapplication1.newfeatures.Chat.Message
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        // Obtener los datos de los pedidos pendientes y eventos desde Firebase
        val dbRef = FirebaseDatabase.getInstance().getReference("Shop")
        dbRef.child("Pending_Orders").get().addOnSuccessListener { ordersSnapshot ->
            val orderEntries = mutableMapOf<String, Int>()
            for (orderSnapshot in ordersSnapshot.children) {
                val productType = orderSnapshot.child("productType").value.toString()
                val status = orderSnapshot.child("status").value.toString()
                if (status == "Accepted") {
                    orderEntries[productType] = orderEntries.getOrDefault(productType, 0) + 1
                }
            }
            val pieEntries = orderEntries.map { PieEntry(it.value.toFloat(), it.key) }
            val pieDataSet = PieDataSet(pieEntries, "Pedidos Aceptados")
            pieDataSet.colors = listOf(Color.BLUE, Color.GREEN, Color.RED)
            pieDataSet.valueTextSize = 20f // Ajusta este valor a tu preferencia
            pieDataSet.valueTextColor = Color.BLACK
            pieDataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            pieDataSet.valueLinePart1OffsetPercentage = 80f // ajusta este valor
            pieDataSet.valueLinePart1Length = 0.5f
            pieDataSet.valueLinePart2Length = 0.4f
            pieDataSet.valueLineColor = Color.BLACK
            pieDataSet.valueLineWidth = 0.5f
            val pieData = PieData(pieDataSet)
            binding.pieChart.data = pieData
            binding.pieChart.description= Description().apply { text = ""}
            binding.pieChart.invalidate() // refresh
        }

        dbRef.child("Events").get().addOnSuccessListener { eventsSnapshot ->
            val eventEntries = mutableMapOf<String, Int>()
            for (eventSnapshot in eventsSnapshot.children) {
                val eventType = eventSnapshot.child("name").value.toString()
                val status = eventSnapshot.child("noti_status").value.toString()
                if (status == "1") {
                    eventEntries[eventType] = eventEntries.getOrDefault(eventType, 0) + 1
                }
            }
            val pieEntries = eventEntries.map { PieEntry(it.value.toFloat(), it.key) }

            val pieDataSetEvent = PieDataSet(pieEntries, "Eventos")
            pieDataSetEvent.colors = listOf(Color.BLUE, Color.GREEN, Color.RED)
            pieDataSetEvent.valueTextSize = 20f // Ajusta este valor a tu preferencia
            pieDataSetEvent.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            pieDataSetEvent.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            pieDataSetEvent.valueLinePart1OffsetPercentage = 80f // ajusta este valor
            pieDataSetEvent.valueLinePart1Length = 0.5f
            pieDataSetEvent.valueLinePart2Length = 0.4f
            pieDataSetEvent.valueLineColor = Color.BLACK
            pieDataSetEvent.valueLineWidth = 0.5f
            val pieDataEvent = PieData(pieDataSetEvent)
            binding.pieChartEvent.data = pieDataEvent
            binding.pieChart.description= Description().apply { text = ""}
            binding.pieChartEvent.invalidate() // refresh
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}