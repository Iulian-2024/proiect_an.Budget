package com.example.gestionare_cheltuieli

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room

import kotlinx.coroutines.launch
import android.os.Bundle as Bundle

class SourceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_list)

        val container = findViewById<LinearLayout>(R.id.sourceListContainer)

        val db = DatabaseProvider.getDatabase(this)


        val sourceDao = db.sourceDao()
        val transactionDao = db.transactionDao()

        lifecycleScope.launch {
            val sources = sourceDao.getAll()
            val transactions = transactionDao.getAll()
            val categorii = db.categorieDao().getAll()
            val categoriiMap = categorii.associateBy { it.id }
            runOnUiThread {
                val titlu = TextView(this@SourceListActivity).apply {
                    text = "Soldurile disponibile"
                    textSize = 18f
                    setPadding(16, 16, 16, 8)
                    setTypeface(null, Typeface.BOLD)
                }
                container.addView(titlu)

                val grupate = sources.groupBy { it.type.uppercase() }

                // Ordinea tipurilor
                val ordine = listOf("CARD", "NUMERAR", "CONTCURENT")
                for (tip in ordine) {
                    val listaSurse = grupate[tip] ?: continue
                    val listaSortata = listaSurse.sortedByDescending { sursa ->
                        transactions.count { it.sourceId == sursa.id }
                    }

                    for (source in listaSortata) {
                        val tranzactii = transactions.filter { it.sourceId == source.id }

                        val venituri = tranzactii
                            .filter { categoriiMap[it.categorieId]?.type == "venit" }
                            .sumOf { it.amount }

                        val cheltuieli = tranzactii
                            .filter { categoriiMap[it.categorieId]?.type == "cheltuiala" }
                            .sumOf { it.amount }

                        val sold = source.initialAmount + venituri - cheltuieli

                        val bloc = LinearLayout(this@SourceListActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(24, 16, 24, 5)
                            setBackgroundResource(R.drawable.source_background) // dacă vrei colțuri rotunjite
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 0)
                            }

                            setOnClickListener {
                                val intent = Intent(this@SourceListActivity, TransactionsBySourceActivity::class.java)
                                intent.putExtra("source_id", source.id)
                                startActivity(intent)
                            }

                            setOnLongClickListener {
                                val intent = Intent(this@SourceListActivity, DeleteSourceActivity::class.java)
                                startActivity(intent)
                                true
                            }

                            val infoLayout = LinearLayout(this@SourceListActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    2f
                                ) // ⬅️ mai mult spațiu
                                addView(TextView(this@SourceListActivity).apply {
                                    text = source.name
                                    textSize = 14f
                                })
                                addView(TextView(this@SourceListActivity).apply {
                                    text = source.type
                                    textSize = 12f
                                })
                            }

                            val suma = TextView(this@SourceListActivity).apply {
                                text = "%.2f lei".format(sold)
                                textSize = 14f
                                gravity = Gravity.END
                                layoutParams = LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                )
                            }

                            addView(infoLayout)
                            addView(suma)
                        }
                        val separator = View(this@SourceListActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1 // grosimea liniei (1 dp)
                            ).apply {
                                topMargin = 8
                                bottomMargin = 8
                            }
                            setBackgroundColor(Color.LTGRAY)
                        }

                        container.addView(bloc)
                        container.addView(separator)
                    }
                }
                val adaugaCont = TextView(this@SourceListActivity).apply {
                    text = "➕ Adaugă cont"
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    setPadding(32, 32, 32, 32)
                    setTextColor(Color.parseColor("#1E88E5")) // albastru
                    gravity = Gravity.CENTER
                    setBackgroundResource(R.drawable.source_background)

                    setOnClickListener {
                        val intent = Intent(this@SourceListActivity, AddSourceActivity::class.java)
                        startActivity(intent)
                    }
                }
                container.addView(adaugaCont)
            }


        }
    }
}
