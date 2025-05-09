package com.nhlstenden.WorkoutNHLStenden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class StartWorkoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button click listener
        view.findViewById<MaterialButton>(R.id.button_start).setOnClickListener {
            Toast.makeText(context, "Starting workout...", Toast.LENGTH_SHORT).show()
            // Here you would normally start the actual workout activity or process
        }
    }
} 