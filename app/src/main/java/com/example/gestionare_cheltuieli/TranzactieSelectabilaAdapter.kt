package com.example.gestionare_cheltuieli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class TranzactieSelectabil(
    val transaction: Transaction,
    val source: Source,
    val category: Categorie
)


class TranzactieSelectabilaAdapter(
    private var transactions: List<TranzactieSelectabil> = emptyList(),
    private val selectedItems: MutableSet<Transaction>
) : RecyclerView.Adapter<TranzactieSelectabilaAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textType: TextView = itemView.findViewById(R.id.textType)
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val textSubcategory: TextView = itemView.findViewById(R.id.textSubcategory)
        val textAmount: TextView = itemView.findViewById(R.id.textAmount)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tranzactie_selectabila, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = transactions[position]
        val transaction = item.transaction

        holder.textType.text = item.source.type
        holder.textCategory.text = item.category.category
        holder.textSubcategory.text = item.category.subcategory
        holder.textAmount.text = "%.2f lei".format(transaction.amount)
        holder.textDate.text = transaction.date

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedItems.contains(transaction)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedItems.add(transaction)
            else selectedItems.remove(transaction)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newList: List<TranzactieSelectabil>) {
        transactions = newList
        notifyDataSetChanged()
    }
}
