package com.example.gestionare_cheltuieli

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch

class TranzactiiActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionDao: TransactionDao
    private lateinit var sourceDao: SourceDao
    private lateinit var categoryDao: CategorieDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tranzactii)

        recyclerView = findViewById(R.id.istoricRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = DatabaseProvider.getDatabase(this)

        transactionDao = db.transactionDao()
        sourceDao = db.sourceDao()
        categoryDao=db.categorieDao()
    }

    override fun onResume() {
                super.onResume()

        lifecycleScope.launch {
            val tranzactii = transactionDao.getAll()
            val surse = sourceDao.getAll()
            val categorii = categoryDao.getAll()

            val elemente = tranzactii.mapNotNull { tranzactie ->
                val sursa = surse.find { s -> s.id == tranzactie.sourceId }
                val categorie = categorii.find { c -> c.id == tranzactie.categorieId }

                if (sursa != null && categorie != null) {
                    TranzactieAfisata(
                        id = tranzactie.id,
                        sourceType = sursa.type,
                        category = categorie.category,
                        amount = tranzactie.amount,
                        date = tranzactie.date,
                        type = categorie.type, // "Venit" / "Cheltuială"
                        subcategory = categorie.subcategory
                    )
                } else null
            }

            runOnUiThread {
                recyclerView.adapter = SelectableTransactionAdapter(elemente)
            }
        }
    }
}
