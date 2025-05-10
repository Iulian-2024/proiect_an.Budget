package com.example.gestionare_cheltuieli

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface ReceiptItemDao {
    @Insert
    suspend fun insertAll(items: List<ReceiptItem>)
}