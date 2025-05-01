@file:Suppress("DEPRECATION")

package com.example.gestionare_cheltuieli

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
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



    private lateinit var totalIncomeText: TextView
    private lateinit var totalExpenseText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        totalIncomeText = findViewById(R.id.textView3)
        totalExpenseText = findViewById(R.id.textView)



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

            lifecycleScope.launch {
                dao.deleteAll()

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

            val incomes = transactions.filter { it.type == "venit" }.map { it.amount }
            val expenses = transactions.filter { it.type == "cheltuială" }.map { it.amount }

            val totalIncome = incomes.sum()
            val totalExpense = expenses.sum()

            val result = transactions.joinToString("\n") {
                "${it.date} - ${it.category.uppercase()}: ${it.amount} lei"
            }

            runOnUiThread {
                totalIncomeText.text = totalIncome.toString() + " lei"
                totalExpenseText.text = totalExpense.toString() + " lei"
                textView.text = if (result.isEmpty()) "Nicio tranzacție" else result
            }
        }
    }






}
