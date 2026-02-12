package com.lemonview.ai.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lemonview.ai.R

class RoutineAdapter(
    private val routineList: List<String>
) : RecyclerView.Adapter<RoutineAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val day: TextView = view.findViewById(R.id.tvDay)
        val routine: TextView = view.findViewById(R.id.tvRoutine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.day.text = "Day ${position + 1}"
        holder.routine.text = routineList[position]
    }

    override fun getItemCount(): Int = routineList.size
}
