package com.example.gestionare_cheltuieli

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: Receipt): Long

    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAll(): LiveData<List<Receipt>>

    @Transaction
    @Query("SELECT * FROM receipts")
    fun getReceiptsWithSuppliers(): List<ReceiptWithSupplier>

}
