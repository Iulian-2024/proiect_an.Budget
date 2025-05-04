package com.example.gestionare_cheltuieli

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,          // ex: "Card ING"
    val type: String,          // ex: "Card", "Cash", "CurrentAccount"
    val initialAmount: Double  // suma de pornire
)