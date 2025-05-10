package com.example.gestionare_cheltuieli


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReceiptAdapter(private val receipts: List<ReceiptWithSupplier>) :
    RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.text_date)
        val supplierText: TextView = itemView.findViewById(R.id.text_supplier)
        val totalText: TextView = itemView.findViewById(R.id.text_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val receiptWithSupplier = receipts[position]
        val receipt = receiptWithSupplier.receipt
        val supplier = receiptWithSupplier.supplier

        holder.dateText.text = "${receipt.date} ${receipt.time}"
        holder.supplierText.text = "Furnizor: ${supplier.name}"
        holder.totalText.text = "Total: ${receipt.total} lei"
    }

    override fun getItemCount(): Int = receipts.size
}
