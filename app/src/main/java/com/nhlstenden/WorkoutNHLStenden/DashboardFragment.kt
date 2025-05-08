package com.nhlstenden.WorkoutNHLStenden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dummy data
        val workouts = listOf(
            mapOf(
                "title" to "Krachttraining",
                "time" to "Vandaag, 10:30",
                "duration" to "45 min",
                "description" to "Benen en core"
            ),
            mapOf(
                "title" to "Cardio",
                "time" to "Gisteren, 18:00",
                "duration" to "30 min",
                "description" to "5 km hardlopen"
            ),
            mapOf(
                "title" to "Yoga",
                "time" to "2 dagen geleden, 08:15",
                "duration" to "60 min",
                "description" to "Ochtend routine"
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = WorkoutAdapter(workouts)

        // Grafiek setup
        val chart = view.findViewById<BarChart>(R.id.barChart)
        val entries = listOf(
            BarEntry(1f, 30f),
            BarEntry(2f, 45f),
            BarEntry(3f, 60f),
            BarEntry(4f, 20f),
            BarEntry(5f, 50f)
        )

        val dataSet = BarDataSet(entries, "Trainingsduur (min)")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        val data = BarData(dataSet)
        chart.data = data
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.invalidate()
    }
}
