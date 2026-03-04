package com.example.barnyhealth

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ListAdapter(
    private val norms: Map<String, Pair<Float, Float>>,
    private val onDelete: (String, Int) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private val items = mutableListOf<ListItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    fun updateItems(newItems: List<ListItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val sdf = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.timestamp))

        // Красный если за референсами
        val norm = norms[item.param] ?: Pair(0f, Float.MAX_VALUE)
        val isOutOfRange = item.value < norm.first || item.value > norm.second
        holder.tvValue.setTextColor(if (isOutOfRange) Color.RED else Color.BLACK)
        holder.tvValue.text = "${item.param}: %.2f".format(item.value)

        holder.btnDelete.setOnClickListener {
            onDelete(item.param, position)
        }
    }


    override fun getItemCount() = items.size

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}
