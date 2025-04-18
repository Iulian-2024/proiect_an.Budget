package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Budget :  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_budget)

        val income = intent.getDoubleExtra("income", 0.0)
        val expense = intent.getDoubleExtra("expense", 0.0)
        val difference = income - expense

        val textBudget = findViewById<TextView>(R.id.textView5)
        textBudget.text = "Diferența: $difference lei"
    }
}