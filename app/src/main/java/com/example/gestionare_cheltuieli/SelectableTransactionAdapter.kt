package com.example.gestionare_cheltuieli

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class TranzactieAfisata(
    val id: Int, // acesta TREBUIE să fie ID-ul TRANZACȚIEI
    val sourceType: String,
    val category: String,
    val subcategory: String,
    val amount: Double,
    val date: String,
    val type: String

)

class SelectableTransactionAdapter(
    private val items: List<TranzactieAfisata>
) : RecyclerView.Adapter<SelectableTransactionAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<Int>()


    fun getSelectedTransactionIds(): List<Int> = selectedItems.toList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val Amount: TextView = itemView.findViewById(R.id.textAmount)
        val Category: TextView = itemView.findViewById(R.id.textCategory)
        val Subcategory: TextView = itemView.findViewById(R.id.textSubcategory)
        val Type: TextView = itemView.findViewById(R.id.textType)
        val Date: TextView = itemView.findViewById(R.id.textDate)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_istoric, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.Amount.text = "${item.amount} lei"
        holder.Category.text = item.category
        holder.Subcategory.text = item.subcategory
        holder.Type.text = item.sourceType
        holder.Date.text = item.date

        holder.itemView.setOnLongClickListener {
            val intent = Intent(holder.itemView.context, TranzactionLook::class.java)
            intent.putExtra("transaction_id", item.id) // dacă vrei să transmiți filtrul
            holder.itemView.context.startActivity(intent)
            true
        }

        if (item.type.lowercase() == "venit") {
            holder.Amount.setTextColor(Color.parseColor("#4CAF50")) // verde
        } else {
            holder.Amount.setTextColor(Color.parseColor("#F44336")) // roșu
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditTransactionActivity::class.java)
            intent.putExtra("transaction_id", item.id)
            holder.itemView.context.startActivity(intent)
        }

    }


    override fun getItemCount(): Int = items.size
}
