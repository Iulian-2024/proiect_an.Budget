package com.example.gestionare_cheltuieli

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

}
