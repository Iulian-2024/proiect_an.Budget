package com.example.gestionare_cheltuieli

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SourceDao {
    @Insert
    suspend fun insert(source: Source)
    @Query("SELECT * FROM sources") suspend fun getAll(): List<Source>
    @Query("SELECT * FROM sources WHERE id = :id") suspend fun getById(id: Int): Source?
    @Delete
    suspend fun delete(source: Source)
    @Update
    suspend fun update(source: Source)

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun count(): Int
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}