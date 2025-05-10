package com.example.gestionare_cheltuieli

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Embedded
import androidx.room.Relation

class TransactionAdapter(
    private val transactions: List<TransactionWithSourceAndCategorie>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.textCategory)
        val tvSubCategory: TextView = itemView.findViewById(R.id.textSubCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.textAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transactions[position]

        val categorie = item.categorie
        val tip = categorie.type.lowercase()

        holder.tvCategory.text = categorie.category
        holder.tvSubCategory.text =  categorie.subcategory
        holder.tvAmount.text = "%.2f lei".format(item.transaction.amount)

        if (tip == "venit") {
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")) // verde
        } else {
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")) // roșu
        }
    }

    override fun getItemCount() = transactions.size
}


data class TransactionWithSourceAndCategorie(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val source: Source,
    @Relation(
        parentColumn = "categorieId",
        entityColumn = "id"
    )
    val categorie: Categorie
)
