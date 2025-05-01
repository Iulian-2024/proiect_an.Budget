package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch

class Venituri :  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_venituri)
        val textView = findViewById<TextView>(R.id.textView6)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        ).build()

        val dao = db.transactionDao()
        lifecycleScope.launch {
            val venituri = dao.getAll().filter { it.type == "venit" }

            val text = if (venituri.isEmpty()) {
                "Nu există venituri înregistrate"
            } else {
                venituri.joinToString("\n") {
                    "${it.date} - ${it.category.uppercase()}: ${it.amount} lei"
                }
            }

            runOnUiThread {
                textView.text = text
            }
        }
    }
}