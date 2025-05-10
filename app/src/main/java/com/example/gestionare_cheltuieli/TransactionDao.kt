package com.example.gestionare_cheltuieli

import androidx.room.*

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Query("""
    SELECT SUM(t.amount) FROM transactions t
    INNER JOIN categorii c ON t.categorieId = c.id
    WHERE c.type = :tip
""")
    suspend fun getTotalByType(tip: String): Double?

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): Transaction?

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int


    @Query("SELECT * FROM transactions")
    suspend fun getAllWithCategorieAndSource(): List<TransactionWithSourceAndCategorie>



}