package com.example.gestionare_cheltuieli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val amountInput = findViewById<TextInputEditText>(R.id.textInputEditText)
        val radioIncome = findViewById<RadioButton>(R.id.radioIncome)
        val button5 = findViewById<Button>(R.id.button5)
        val categorySpinner = findViewById<Spinner>(R.id.spinner)
        val sourceSpinner = findViewById<Spinner>(R.id.sourceInput)

        val categories = listOf("Salariu", "Alocație", "Vânzare", "Bonus", "Dobândă", "Transfer", "Schimb Valutar", "Împrumut", "Transport", "Mâncare", "Altele")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "transactions-db")
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.transactionDao()
        val sourceDao = db.sourceDao()

        lifecycleScope.launch {
            val sources = sourceDao.getAll()
            val sourceAdapter = ArrayAdapter(
                this@NewRecordActivity,
                android.R.layout.simple_spinner_item,
                sources.map { it.name }
            )

            runOnUiThread {
                sourceSpinner.adapter = sourceAdapter

                // MUTA aici listenerul (pe UI thread)
                button5.setOnClickListener {
                    val selectedName = sourceSpinner.selectedItem?.toString()
                    val selectedSource = sources.find { it.name == selectedName }
                    val amountText = amountInput.text.toString()
                    val amount = amountText.toDoubleOrNull()
                    val selectedCategory = categorySpinner.selectedItem.toString()

                    if (selectedSource == null) {
                        Toast.makeText(this@NewRecordActivity, "Selectează o sursă validă", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (amount != null && amount > 0) {
                        val type = if (radioIncome.isChecked) "venit" else "cheltuială"
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val currentDate = dateFormat.format(Date())

                        val transaction = Transaction(
                            amount = amount,
                            type = type,
                            category = selectedCategory,
                            description = "",
                            date = currentDate,
                            sourceId = selectedSource.id
                        )

                        lifecycleScope.launch {
                            dao.insert(transaction)
                            runOnUiThread {
                                Toast.makeText(this@NewRecordActivity, "Tranzacție salvată", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@NewRecordActivity, "Introdu o sumă validă", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
