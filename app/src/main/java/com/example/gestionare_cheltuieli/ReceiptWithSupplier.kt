package com.example.gestionare_cheltuieli

import androidx.room.Embedded
import androidx.room.Relation

data class ReceiptWithSupplier(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "supplier_id",
        entityColumn = "id"
    )
    val supplier: Supplier
)
