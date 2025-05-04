@file:Suppress("DEPRECATION")

package com.example.gestionare_cheltuieli

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.w3c.dom.Text as Text1

class MainActivity : AppCompatActivity() {



    //private lateinit var totalIncomeText: TextView
    //private lateinit var totalExpenseText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        //totalIncomeText = findViewById(R.id.textView3)
        //totalExpenseText = findViewById(R.id.textView)



        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            val intent = Intent(this, NewRecordActivity::class.java)
            startActivityForResult(intent, 1)
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {



            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "transactions-db"
            )
                .fallbackToDestructiveMigration()
                .build()

            val dao = db.transactionDao()

            lifecycleScope.launch {
                db.transactionDao().deleteAll()
                ExcelImporter.importSources(this@MainActivity, db)
                val nrSurse = db.sourceDao().count()

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sursele au fost importate ($nrSurse)!", Toast.LENGTH_SHORT).show()
                }

                val surse = db.sourceDao().getAll()
                surse.forEach {
                    Log.d("SOURCE", "ID=${it.id} | ${it.name} (${it.type}) - ${it.initialAmount} lei")
                }
            }

            lifecycleScope.launch {
                db.sourceDao().deleteAll()
                ExcelImporter.importTransactions(this@MainActivity, db)
                val nrTranzactii = db.transactionDao().count()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Tranzacțiile au fost importate ($nrTranzactii)!", Toast.LENGTH_SHORT).show()
                }
                val tranzactii = db.transactionDao().getAll()
                tranzactii.forEach {
                    //Log.d("TRANSACTION", "ID=${it.id} | ${it.date} - ${it.type.uppercase()} ${it.amount} lei | Cat=${it.category} | SourceId=${it.sourceId}")
                }
            }


            lifecycleScope.launch {
                val transactions = dao.getAll()



                val incomeTotal = transactions.filter { it.type == "venit" }.sumOf { it.amount }
                val expenseTotal = transactions.filter { it.type == "cheltuială" }.sumOf { it.amount }

                val intent = Intent(this@MainActivity, Budget::class.java).apply {
                    putExtra("income", incomeTotal)
                    putExtra("expense", expenseTotal)
                }


                startActivity(intent)
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

        val textView = findViewById<TextView>(R.id.transactionListTextView)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.transactionDao()

        textView.setOnClickListener {
            val intent = Intent(this, TranzactionLook::class.java)
            startActivity(intent)
        }

        val sourceDao = db.sourceDao()
        val transactionDao = db.transactionDao()
        val layout = findViewById<LinearLayout>(R.id.sourceTotalsLayout) // layoutul în care afișăm





        lifecycleScope.launch {

            val sources = sourceDao.getAll()
            val transactions = transactionDao.getAll()



            runOnUiThread {
                layout.removeAllViews()

                // Grupare și calcul total per tip (Card, Cash, etc.)
                val sourcesByType = sources.groupBy { it.type }
                val totalPerType = sourcesByType.mapValues { (_, surse) ->
                    surse.sumOf { source ->
                        val sourceTransactions = transactions.filter { it.sourceId == source.id }
                        val venituri = sourceTransactions.filter { it.type == "Venit" }.sumOf { it.amount }
                        val cheltuieli = sourceTransactions.filter { it.type == "Cheltuiala" }.sumOf { it.amount }
                        source.initialAmount + venituri - cheltuieli
                    }
                }

                // Convertim în listă de perechi (tip, sumă)
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

                            // Poți adăuga funcție de click dacă vrei
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

        lifecycleScope.launch {
            val transactions = dao.getAll()
            val formatted = transactions.joinToString("\n") { tranzactie ->
                "${tranzactie.date} - ${tranzactie.category.uppercase()}: ${tranzactie.amount} lei"
            }

            runOnUiThread {
                textView.text = if (formatted.isEmpty()) "Nicio tranzacție" else formatted
            }
        }

        val clearButton = findViewById<Button>(R.id.button6) // presupunem că ai un buton cu acest ID

        clearButton.setOnClickListener {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "transactions-db"
            )
                .fallbackToDestructiveMigration()
                .build()

            val dao = db.transactionDao()
            val ds=db.sourceDao()

            lifecycleScope.launch {
                dao.deleteAll()
                ds.deleteAll()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Toate tranzacțiile au fost șterse", Toast.LENGTH_SHORT).show()
                    updateUI() // dacă ai o funcție care reafișează UI-ul
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.transactionDao()
        val textView = findViewById<TextView>(R.id.transactionListTextView)

        lifecycleScope.launch {
            val transactions = dao.getAll()

            val incomes = transactions.filter { it.type == "Venit" }.map { it.amount }
            val expenses = transactions.filter { it.type == "Cheltuiala" }.map { it.amount }

            val totalIncome = incomes.sum()
            val totalExpense = expenses.sum()

            val result = transactions.joinToString("\n") {
                "${it.date} - ${it.category.uppercase()}: ${it.amount} lei"
            }

            runOnUiThread {
                //totalIncomeText.text = totalIncome.toString() + " lei"
                //totalExpenseText.text = totalExpense.toString() + " lei"
                textView.text = if (result.isEmpty()) "Nicio tranzacție" else result
            }
        }

    }






}
