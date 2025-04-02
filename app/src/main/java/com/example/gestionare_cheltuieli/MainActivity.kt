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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text as Text1

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val incomes = listOf(1000.0, 2500.0, 750.0)
        val expenses = listOf(500.0, 1200.0, 300.0)
        val totalIncome = incomes.sum()
        val totalExpense = expenses.sum()
        Log.i("Venituri", totalIncome.toString())
        Log.i("Cheltuieli", totalExpense.toString())
        val budget = totalIncome-totalExpense
        Log.i("Budget",budget.toString())
        var totalIncomeText = findViewById<TextView>(R.id.textView)
        totalIncomeText.text= totalIncome.toString()
        val totalExpenseText = findViewById<TextView>(R.id.textView3)
        totalExpenseText.text= totalExpense.toString()


        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, Budget::class.java)
            startActivity(intent)

        }
    }
}