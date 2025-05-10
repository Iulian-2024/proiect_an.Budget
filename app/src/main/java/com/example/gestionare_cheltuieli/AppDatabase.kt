package com.example.gestionare_cheltuieli

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class, Source::class, Categorie::class,
    Supplier::class, Receipt::class, ReceiptItem::class], version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun sourceDao(): SourceDao
    abstract fun categorieDao(): CategorieDao
    abstract fun supplierDao(): SupplierDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun receiptItemDao(): ReceiptItemDao

}
