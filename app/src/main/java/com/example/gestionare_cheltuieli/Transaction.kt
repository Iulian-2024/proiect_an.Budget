package com.example.gestionare_cheltuieli

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")

data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // „income” sau „expense”
    val category: String, // 🔹 Nou câmp
    val description: String,
    val date: String,
    val sourceId: Int
)