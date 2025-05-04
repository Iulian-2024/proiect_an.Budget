package com.example.gestionare_cheltuieli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch

class TranzactiiActivity : AppCompatActivity() {
    private lateinit var dao: TransactionDao
    private val selectedItems = mutableSetOf<Transaction>()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tranzactii)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val deleteButton = findViewById<Button>(R.id.deleteButton)



        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        )
            .fallbackToDestructiveMigration()
            .build()
        dao = db.transactionDao()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter(
            emptyList(),
            selectedItems,
            onItemClick = { tranzactie ->
                val intent = Intent(this, EditTransactionActivity::class.java)
                intent.putExtra("transaction_id", tranzactie.id)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        loadTransactions()

        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                selectedItems.forEach { dao.deleteTransaction(it) }
                selectedItems.clear()
                loadTransactions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            val all = dao.getAll()

            Log.d("lista",all.toString())
            Log.d("DEBUG", "Total tranzacții: ${all.size}")

            runOnUiThread {
                adapter.updateData(all)
            }
        }
    }
}