package com.nhlstenden.WorkoutNHLStenden

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val workouts: List<Map<String, String>>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.tvTitle)
        val time = itemView.findViewById<TextView>(R.id.tvTime)
        val duration = itemView.findViewById<TextView>(R.id.tvDuration)
        val description = itemView.findViewById<TextView>(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.title.text = workout["title"]
        holder.time.text = workout["time"]
        holder.duration.text = workout["duration"]
        holder.description.text = workout["description"]
    }

    override fun getItemCount() = workouts.size
}
