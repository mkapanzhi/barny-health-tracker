package com.example.barnyhealth

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeasurementAdapter(
    private var items: List<MeasurementItem>,
    private val onItemLongPress: (MeasurementItem) -> Unit,
    private val onDeleteClick: (MeasurementItem) -> Unit,
    private val onItemClick: (MeasurementItem) -> Unit
) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    class MeasurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemDate: TextView = itemView.findViewById(R.id.tvItemDate)
        val tvItemParam: TextView = itemView.findViewById(R.id.tvItemParam)
        val tvItemValue: TextView = itemView.findViewById(R.id.tvItemValue)
        val btnDeleteMeasurement: ImageButton =
            itemView.findViewById(R.id.btnDeleteMeasurement)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_measurement, parent, false)
        return MeasurementViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        val item = items[position]

        holder.tvItemDate.text = item.date
        holder.tvItemParam.text = item.paramName
        holder.tvItemValue.text = item.valueText

        holder.tvItemValue.setTextColor(
            if (item.isOutOfNorm) Color.parseColor("#D32F2F")
            else Color.parseColor("#23A26D")
        )

        holder.btnDeleteMeasurement.visibility =
            if (item.showDelete) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            onItemLongPress(item)
            true
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.btnDeleteMeasurement.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MeasurementItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun showDeleteFor(timestamp: Long) {
        items = items.map {
            it.copy(showDelete = it.timestamp == timestamp)
        }
        notifyDataSetChanged()
    }

    fun hideDeleteButtons() {
        val hasVisibleDelete = items.any { it.showDelete }
        if (!hasVisibleDelete) return

        items = items.map { it.copy(showDelete = false) }
        notifyDataSetChanged()
    }
}