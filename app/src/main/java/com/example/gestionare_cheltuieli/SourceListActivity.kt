package com.example.gestionare_cheltuieli

import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room

import kotlinx.coroutines.launch
import java.io.File
import android.os.Bundle as Bundle

class SourceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_list)

        val container = findViewById<LinearLayout>(R.id.sourceListContainer)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "transactions-db")
            .fallbackToDestructiveMigration()
            .build()

        val sourceDao = db.sourceDao()
        val transactionDao = db.transactionDao()

        lifecycleScope.launch {
            val sources = sourceDao.getAll()
            val transactions = transactionDao.getAll()





            runOnUiThread {
                for (source in sources) {
                    val tranzactii = transactions.filter { it.sourceId == source.id }
                    val venituri = tranzactii.filter { it.type == "Venit" }.sumOf { it.amount }
                    val cheltuieli = tranzactii.filter { it.type == "Cheltuiala" }.sumOf { it.amount }
                    val sold = source.initialAmount + venituri - cheltuieli

                    val textView = TextView(this@SourceListActivity).apply {
                        text = "${source.type.uppercase()} – ${source.name}: %.2f lei".format(sold)
                        textSize = 16f
                        setPadding(16, 16, 16, 16)
                    }

                    container.addView(textView)
                }
            }
        }
    }
}
