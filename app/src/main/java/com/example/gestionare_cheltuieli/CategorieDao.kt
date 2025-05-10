package com.example.gestionare_cheltuieli

import androidx.room.*

@Dao
interface CategorieDao {
    @Insert
    suspend fun insert(categorie: Categorie)

    @Update
    suspend fun update(categorie: Categorie)

    @Delete
    suspend fun delete(categorie: Categorie)

    @Query("DELETE FROM categorii")
    suspend fun deleteAll()

    @Query("SELECT * FROM categorii")
    suspend fun getAll(): List<Categorie>

    @Query("SELECT * FROM categorii WHERE id = :id")
    suspend fun getCategorieById(id: Int): Categorie

    @Query("SELECT * FROM categorii WHERE type = :type")
    suspend fun getByTip(type: String): List<Categorie>
}
