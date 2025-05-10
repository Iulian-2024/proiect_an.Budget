package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TransactionsBySourceActivity : AppCompatActivity() {

    private lateinit var transactionDao: TransactionDao
    private lateinit var categorieDao: CategorieDao
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_by_source)

        val sourceId = intent.getIntExtra("source_id", -1)
        if (sourceId == -1) {
            Toast.makeText(this, "Sursă invalidă", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewTransactionsBySource)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = DatabaseProvider.getDatabase(this)
        transactionDao = db.transactionDao()
        categorieDao = db.categorieDao()

        lifecycleScope.launch {
            val tranzactii = transactionDao.getAll().filter { it.sourceId == sourceId }
            val categorii = categorieDao.getAll()

            val afisate = tranzactii.mapNotNull { t ->
                val cat = categorii.find { it.id == t.categorieId }
                if (cat != null) {
                    TranzactieAfisata(
                        id = t.id,
                        sourceType = "", // deja știi sursa
                        category = cat.category,
                        subcategory = cat.subcategory,
                        amount = t.amount,
                        date = t.date,
                        type = cat.type
                    )
                } else null
            }

            runOnUiThread {
                recyclerView.adapter = SelectableTransactionAdapter(afisate)
            }
        }
    }
}
