package com.example.gestionare_cheltuieli

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var dao: TransactionDao
    private var transactionId: Int = -1
    private lateinit var transaction: Transaction

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val amountInput = findViewById<TextInputEditText>(R.id.textInputEditText)
        val radioIncome = findViewById<RadioButton>(R.id.radioIncome)
        val radioExpense = findViewById<RadioButton>(R.id.radioExpense)
        val dateInput = findViewById<TextInputEditText>(R.id.dateInputEditTex)
        val spinner = findViewById<Spinner>(R.id.spinner)
        val button = findViewById<Button>(R.id.button5)

        val categories = listOf("Salariu", "Mâncare", "Transport", "Alocație", "Altele")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        dao = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        )
            .fallbackToDestructiveMigration()
            .build().transactionDao()

        transactionId = intent.getIntExtra("transaction_id", -1)

        lifecycleScope.launch {
            val t = dao.getById(transactionId)
            if (t != null) {
                transaction = t
                runOnUiThread {
                    amountInput.setText(t.amount.toString())
                    dateInput.setText(t.date)
                    if (t.type == "income") radioIncome.isChecked = true else radioExpense.isChecked = true
                    spinner.setSelection(getCategoryIndex(spinner, t.category))
                }
            }
        }

        button.text = "Salvează"
        button.setOnClickListener {
            val newAmount = amountInput.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val newDate = dateInput.text.toString()
            val newCategory = spinner.selectedItem.toString()
            val newType = if (radioIncome.isChecked) "income" else "expense"

            val updated = transaction.copy(
                amount = newAmount,
                date = newDate,
                category = newCategory,
                type = newType
            )

            lifecycleScope.launch {
                dao.updateTransaction(updated)
                runOnUiThread {
                    Toast.makeText(this@EditTransactionActivity, "Modificat cu succes", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun getCategoryIndex(spinner: Spinner, category: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == category) return i
        }
        return 0
    }
}
