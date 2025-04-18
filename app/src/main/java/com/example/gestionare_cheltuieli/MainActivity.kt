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
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Text as Text1

class MainActivity : AppCompatActivity() {

    private val incomes = mutableListOf(1000.0, 2500.0, 750.0)
    private val expenses = mutableListOf(500.0, 1200.0, 300.0)

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

        totalIncomeText = findViewById(R.id.textView)
        totalExpenseText = findViewById(R.id.textView3)

        updateUI()

        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener {
            val intent = Intent(this, NewRecordActivity::class.java)
            startActivityForResult(intent, 1)
        }

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, Budget::class.java)
            startActivity(intent)
            intent.putExtra("income", incomes.sum())
            intent.putExtra("expense", expenses.sum())
            startActivity(intent)
        }

        val B_venituri = findViewById<Button>(R.id.button3)
        B_venituri.setOnClickListener {
            val intent2 = Intent(this, Venituri::class.java)
            startActivity(intent2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val amount = data?.getDoubleExtra("amount", 0.0) ?: return
            val type = data.getStringExtra("type")

            if (type == "income") {
                incomes.add(amount)
            } else {
                expenses.add(amount)
            }

            updateUI()
        }
    }

    private fun updateUI() {
        val totalIncome = incomes.sum()
        val totalExpense = expenses.sum()
        totalIncomeText.text = totalIncome.toString()
        totalExpenseText.text = totalExpense.toString()
    }

}
