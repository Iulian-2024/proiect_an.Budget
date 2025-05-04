package com.example.gestionare_cheltuieli

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class, Source::class], version = 7)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun sourceDao(): SourceDao

}
