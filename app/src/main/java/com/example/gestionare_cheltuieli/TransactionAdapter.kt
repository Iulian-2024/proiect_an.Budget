package com.example.gestionare_cheltuieli

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<Transaction> = emptyList(),
    private val selectedItems: MutableSet<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.checkBox.text = "${transaction.date} - ${transaction.category}: ${transaction.amount} lei"

        holder.checkBox.setOnCheckedChangeListener(null) // previne bug de reciclare
        holder.checkBox.isChecked = selectedItems.contains(transaction)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(transaction)
            } else {
                selectedItems.remove(transaction)
            }
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged()
    }
}