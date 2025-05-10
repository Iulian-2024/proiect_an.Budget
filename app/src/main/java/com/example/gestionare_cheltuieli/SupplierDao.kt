package com.example.gestionare_cheltuieli

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(supplier: Supplier): Long

    @Query("SELECT * FROM suppliers WHERE name = :name AND fiscal_code = :fiscalCode LIMIT 1")
    suspend fun getByNameAndFiscalCode(name: String, fiscalCode: String): Supplier?
}
