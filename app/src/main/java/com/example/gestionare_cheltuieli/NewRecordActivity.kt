package com.example.gestionare_cheltuieli

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewRecordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val amountInput = findViewById<TextInputEditText>(R.id.textInputEditText)
        val radioIncome = findViewById<RadioButton>(R.id.radioIncome)
        val button5 = findViewById<Button>(R.id.button5)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions-db"
        ).build()
        val dao = db.transactionDao()

        button5.setOnClickListener {
            val amountText = amountInput.text.toString()
            val amount = amountText.toDoubleOrNull()

            if (amount != null && amount > 0) {
                val type = if (radioIncome.isChecked) "venit" else "cheltuială"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val transaction = Transaction(
                    amount = amount,
                    type = type,
                    description = "", // poți adăuga câmp pentru descriere mai târziu
                    date = currentDate
                )

                Log.d("DB_TEST", "Tranzacție salvată: $transaction")

                lifecycleScope.launch {
                    dao.insert(transaction)
                    runOnUiThread {
                        Toast.makeText(this@NewRecordActivity, "Tranzacție salvată", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Introdu o sumă validă", Toast.LENGTH_SHORT).show()
            }

        }
    }
}