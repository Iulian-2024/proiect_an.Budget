package com.example.gestionare_cheltuieli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class NewRecordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val amountInput = findViewById<TextInputEditText>(R.id.textInputEditText)
        val radioIncome = findViewById<RadioButton>(R.id.radioIncome)
        val button5 = findViewById<Button>(R.id.button5)

        button5.setOnClickListener {
            val amountText = amountInput.text.toString()
            val amount = amountText.toDoubleOrNull()

            if (amount != null) {
                val resultIntent = Intent().apply {
                    putExtra("amount", amount)
                    putExtra("type", if (radioIncome.isChecked) "income" else "expense")
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Introdu o sumă validă", Toast.LENGTH_SHORT).show()
            }
        }
    }
}