package com.example.gestionare_cheltuieli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectableSourceAdapter(
    private val sources: List<Source>
) : RecyclerView.Adapter<SelectableSourceAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()

    fun getSelectedSourceIds(): List<Int> = selectedItems.toList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvSourceName)
        val type: TextView = itemView.findViewById(R.id.tvSourceType)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBoxSource)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_source_with_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = sources.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = sources[position]
        holder.name.text = source.name
        holder.type.text = source.type
        holder.checkBox.isChecked = selectedItems.contains(source.id)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(source.id)
            } else {
                selectedItems.remove(source.id)
            }
        }
    }
}
