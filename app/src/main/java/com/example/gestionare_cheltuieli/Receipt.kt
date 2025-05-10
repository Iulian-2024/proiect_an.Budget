package com.example.gestionare_cheltuieli

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    foreignKeys = [ForeignKey(
        entity = Supplier::class,
        parentColumns = ["id"],
        childColumns = ["supplier_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "supplier_id") val supplierId: Int,
    @ColumnInfo(name = "receipt_number") val receiptNumber: String,
    val date: String,
    val time: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: String,
    val total: Double
)

