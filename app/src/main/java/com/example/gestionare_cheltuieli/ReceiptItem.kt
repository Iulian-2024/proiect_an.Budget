package com.example.gestionare_cheltuieli

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipt_items",
    foreignKeys = [ForeignKey(
        entity = Receipt::class,
        parentColumns = ["id"],
        childColumns = ["receipt_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ReceiptItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "receipt_id") val receiptId: Int,
    @ColumnInfo(name = "product_name") val productName: String,
    val unit: String?,  // poate fi null
    val quantity: Double,
    @ColumnInfo(name = "unit_price") val unitPrice: Double,
    val cost: Double  // quantity * unit_price
)
