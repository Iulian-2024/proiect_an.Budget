package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Venituri :  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_venituri)
        var textVenituri = findViewById<TextView>(R.id.textView6)
        textVenituri.text="10500"
    }
}