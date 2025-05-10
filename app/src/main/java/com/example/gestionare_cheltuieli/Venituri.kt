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
        val db = DatabaseProvider.getDatabase(this)


        val transactionDao = db.transactionDao()
        val categorieDao = db.categorieDao()


        lifecycleScope.launch {
            val tranzactii = transactionDao.getAll()
            val categorii = categorieDao.getAll()
            val categoriiMap = categorii.associateBy { it.id }



            val venituri = tranzactii.filter {
                val cat = categoriiMap[it.categorieId]
                cat?.type?.lowercase() == "venit"
            }


            val totaluriPeCategorii = venituri
                .groupBy { categoriiMap[it.categorieId]?.category ?: "Necunoscut" }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val text = if (totaluriPeCategorii.isEmpty()) {
                "Nu există venituri înregistrate"
            } else {
                totaluriPeCategorii.entries.joinToString("\n") { (categorie, suma) ->
                    "${categorie.uppercase()}: %.2f lei".format(suma)
                }
            }


            runOnUiThread {
                textView.text = text
            }
        }
    }
}