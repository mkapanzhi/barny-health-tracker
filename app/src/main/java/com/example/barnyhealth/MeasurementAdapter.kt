package com.example.barnyhealth

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeasurementAdapter(
    private var items: List<MeasurementItem>
) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    class MeasurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemDate: TextView = itemView.findViewById(R.id.tvItemDate)
        val tvItemParam: TextView = itemView.findViewById(R.id.tvItemParam)
        val tvItemValue: TextView = itemView.findViewById(R.id.tvItemValue)
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
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MeasurementItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}