package com.example.barnyhealth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MeasurementAdapter(
    private val measurements: MutableList<MeasurementItem>,
    private val onDelete: (MeasurementItem) -> Unit
) : RecyclerView.Adapter<MeasurementAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvParam: TextView = view.findViewById(R.id.tvItemParam)
        val tvValue: TextView = view.findViewById(R.id.tvItemValue)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = measurements[position]

        holder.tvDate.text = item.date
        holder.tvParam.text = item.param
        holder.tvValue.text = if (item.unit.isBlank()) {
            item.value
        } else {
            "${item.value} ${item.unit}"
        }

        val valueColor = if (item.isOutOfNorm) {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
        } else {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
        }
        holder.tvValue.setTextColor(valueColor)

        holder.btnMenu.setOnClickListener { view ->
            showPopupMenu(view, item)
        }
    }

    private fun showPopupMenu(view: View, item: MeasurementItem) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.item_actions, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onDelete(item)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = measurements.size

    fun updateItems(newItems: List<MeasurementItem>) {
        measurements.clear()
        measurements.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: MeasurementItem) {
        val position = measurements.indexOf(item)
        if (position != -1) {
            measurements.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}