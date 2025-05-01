package com.example.gestionare_cheltuieli

import androidx.room.*

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    suspend fun getTotalByType(type: String): Double?

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)


}