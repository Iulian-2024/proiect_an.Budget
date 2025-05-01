package com.example.gestionare_cheltuieli

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch

class TranzactionLook: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.look_tranzaction)

        val textView = findViewById<TextView>(R.id.tvListaTranzactii)

        textView.setOnClickListener {
            val intent = Intent(this, TranzactiiActivity::class.java)
            startActivity(intent)
        }
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        ).build()
        val dao = db.transactionDao()

        lifecycleScope.launch {
            val tranzactii = dao.getAll()
            val text = if (tranzactii.isEmpty()) {
                "Nicio tranzacție înregistrată"
            } else {
                tranzactii.joinToString("\n") {
                    "${it.date} - ${it.category.uppercase()}: ${it.amount} lei"
                }
            }

            runOnUiThread {
                textView.text = text
            }
        }
    }
}