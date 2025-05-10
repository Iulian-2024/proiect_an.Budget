package com.example.gestionare_cheltuieli

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorii")
data class Categorie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,                 // "Venit" sau "Cheltuială"
    val category: String,          // ex: "Transport"
    val subcategory: String,       // ex: "Combustibil"
    val ex_use: String?  // ex: "Plată benzină OMV" (opțional)
)