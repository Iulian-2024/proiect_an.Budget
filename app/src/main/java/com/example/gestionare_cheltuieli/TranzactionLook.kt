package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch


class TranzactionLook : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dao: TransactionDao
    private lateinit var sourceDao: SourceDao
    private lateinit var categorieDao: CategorieDao

    private val selectedItems = mutableSetOf<Transaction>()
    private lateinit var adapter: TranzactieSelectabilaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.look_tranzaction)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val deleteButton = findViewById<Button>(R.id.deleteButton)

        val db = DatabaseProvider.getDatabase(this)


        dao = db.transactionDao()
        sourceDao = db.sourceDao()
        categorieDao = db.categorieDao()

        loadTransactions()

        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                selectedItems.forEach { dao.deleteTransaction(it) }
                selectedItems.clear()
                loadTransactions()
            }
        }
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            val tranzactii = dao.getAll()
            val sources = sourceDao.getAll()
            val categorii = categorieDao.getAll()

            val listaAfisabila = tranzactii.mapNotNull { tranzactie ->
                val sursa = sources.find { it.id == tranzactie.sourceId }
                val categorie = categorii.find { it.id == tranzactie.categorieId }

                if (sursa != null && categorie != null) {
                    TranzactieSelectabil(tranzactie, sursa, categorie)
                } else null
            }

            runOnUiThread {
                adapter = TranzactieSelectabilaAdapter(listaAfisabila, selectedItems)
                recyclerView.adapter = adapter
            }
        }
    }
}
