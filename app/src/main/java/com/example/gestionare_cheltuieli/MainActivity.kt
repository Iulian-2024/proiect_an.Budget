@file:Suppress("DEPRECATION")

package com.example.gestionare_cheltuieli

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch



class MainActivity : AppCompatActivity() {

    private suspend fun reinicializeazaDate(db: AppDatabase) {
        db.sourceDao().deleteAll()
        db.transactionDao().deleteAll()
        db.categorieDao().deleteAll()

        ExcelImporter.importSources(this@MainActivity, db)
        ExcelImporter.importCategories(this@MainActivity, db)
        ExcelImporter.importTransactions(this@MainActivity, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        val db = DatabaseProvider.getDatabase(this)
        val dao = db.transactionDao()
        val sourceDao = db.sourceDao()
        val categorieDao = db.categorieDao()

        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            val intent = Intent(this, QRReceiptScannerActivity::class.java)
            startActivityForResult(intent, 1)
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            lifecycleScope.launch {
                reinicializeazaDate(db)

                val lista = dao.getAllWithCategorieAndSource()
                lista.forEach {
                    Log.d("TEST", "${it.transaction.date}: ${it.categorie.category} (${it.categorie.type})")
                }
                val nrTranzactii = dao.count()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Import complet ($nrTranzactii tranzacții)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val B_venituri = findViewById<Button>(R.id.button3)
        B_venituri.setOnClickListener {
            val intent2 = Intent(this, Venituri::class.java)
            startActivity(intent2)
        }

        val B_cheltuieli = findViewById<Button>(R.id.button2)
        B_cheltuieli.setOnClickListener {
            val intent3 = Intent(this, Cheltuieli::class.java)
            startActivity(intent3)
        }

        val layout = findViewById<LinearLayout>(R.id.sourceTotalsLayout)

        lifecycleScope.launch {
            val sources = sourceDao.getAll()
            val transactions = dao.getAll()
            val categorii = categorieDao.getAll()

            runOnUiThread {
                layout.removeAllViews()

                val categoriiMap = categorii.associateBy { it.id }
                val sourcesByType = sources.groupBy { it.type }

                val totalPerType = sourcesByType.mapValues { (_, surse) ->
                    surse.sumOf { source ->
                        val sourceTransactions = transactions.filter { it.sourceId == source.id }

                        val venituri = sourceTransactions.filter {
                            val cat = categoriiMap[it.categorieId]
                            cat?.type == "venit"
                        }.sumOf { it.amount }

                        val cheltuieli = sourceTransactions.filter {
                            val cat = categoriiMap[it.categorieId]
                            cat?.type == "cheltuiala"
                        }.sumOf { it.amount }

                        source.initialAmount + venituri - cheltuieli
                    }
                }

                val totaluriList = totalPerType.entries.toList()

                for (i in totaluriList.indices step 2) {
                    val rowLayout = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    for (j in 0..1) {
                        val index = i + j
                        if (index < totaluriList.size) {
                            val (tip, sold) = totaluriList[index]

                            val titlu = TextView(this@MainActivity).apply {
                                text = tip
                                textSize = 16f
                                setTypeface(null, Typeface.BOLD)
                                gravity = Gravity.CENTER
                            }

                            val suma = TextView(this@MainActivity).apply {
                                text = "%.2f lei".format(sold)
                                textSize = 14f
                                gravity = Gravity.CENTER
                            }

                            val bloc = LinearLayout(this@MainActivity).apply {
                                orientation = LinearLayout.VERTICAL
                                setPadding(16, 16, 16, 16)
                                gravity = Gravity.CENTER
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                                addView(titlu)
                                addView(suma)
                            }

                            bloc.setOnClickListener {
                                val intent = Intent(this@MainActivity, SourceListActivity::class.java)
                                intent.putExtra("sourceType", tip)
                                startActivity(intent)
                            }

                            rowLayout.addView(bloc)
                        }
                    }

                    layout.addView(rowLayout)
                }
            }
        }

        val clearButton = findViewById<Button>(R.id.button6)
        clearButton.setOnClickListener {
            lifecycleScope.launch {
                dao.deleteAll()
                sourceDao.deleteAll()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Toate tranzacțiile au fost șterse", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val db = DatabaseProvider.getDatabase(this)
        val dao = db.transactionDao()
        val sourceDao = db.sourceDao()
        val categorieDao = db.categorieDao()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val transactions = dao.getAll()
            val sources = sourceDao.getAll()
            val categorii = categorieDao.getAll()

            val withSourceAndCategorie = transactions.map { t ->
                val source = sources.find { it.id == t.sourceId } ?: Source(0, "Necunoscut", "", 0.0)
                val categorie = categorii.find { it.id == t.categorieId } ?: Categorie(0, "Necunoscut", "Fără categorie", "", null)
                TransactionWithSourceAndCategorie(t, source, categorie)
            }

            runOnUiThread {
                recyclerView.adapter = TransactionAdapter(withSourceAndCategorie)
            }
        }

        val tvVeziIstoricul = findViewById<TextView>(R.id.tvVeziIstoricul)
        tvVeziIstoricul.setOnClickListener {
            val intent = Intent(this, TranzactiiActivity::class.java)
            startActivityForResult(intent, 123)
        }
    }
}