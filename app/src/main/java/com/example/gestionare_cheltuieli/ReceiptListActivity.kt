package com.example.gestionare_cheltuieli

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ReceiptListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceiptAdapter
    val db = DatabaseProvider.getDatabase(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_list)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val receipts = db.receiptDao().getReceiptsWithSuppliers()
            adapter = ReceiptAdapter(receipts)
            recyclerView.adapter = adapter
        }
    }

}
