package com.example.gestionare_cheltuieli

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object  ExcelImporter {
    suspend fun importSources(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "Surce.txt")
        Log.i("CSV_IMPORT", "Fișier surse.csv găsit: ${file}")
        if (!file.exists()) return@withContext


        file.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    val name = parts[0].trim()
                    val type = parts[1].trim()
                    val amount = parts[2].trim().toDoubleOrNull() ?: 0.0

                    val source = Source(name = name, type = type, initialAmount = amount)
                    db.sourceDao().insert(source)

                    Log.d("sursa", "I| ${name} (${type}) - ${amount}")
                }
            }
        }
    }
    suspend fun importCategories(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "Category.txt")
        Log.i("CSV_IMPORT", "Fișier Category.txt găsit: $file")

        if (!file.exists()) return@withContext

        file.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 3) {
                    val type = parts[0].trim()
                    val category = parts[1].trim()
                    val subcategory = parts[2].trim()

                    val categorieEntity = Categorie(
                        type = type,
                        category = category,
                        subcategory = subcategory,
                        ex_use = null // nu există în fișier, deci null
                    )

                    db.categorieDao().insert(categorieEntity)
                    Log.d("CATEGORIE_IMPORT", "Importat: $type | $category > $subcategory")
                }
            }
        }
    }


    suspend fun importTransactions(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
            val file = File(context.filesDir,"Tranzactii.txt")
            if (!file.exists()) return@withContext

            file.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split("|")
                    if (parts.size >= 4) {
                        val amount = parts[0].trim().toDoubleOrNull() ?: return@forEach
                        val categoryId = parts [3].trim().toIntOrNull() ?: return@forEach
                        val sourceId = parts[2].trim().toIntOrNull() ?: return@forEach
                        val rawDate = parts[1].trim()
                        val formattedDate = try {
                            val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            outputFormat.format(inputFormat.parse(rawDate)!!)
                        } catch (e: Exception) {
                            return@forEach // ignoră rândul dacă data e invalidă
                        }

                        val transaction = Transaction(
                            amount = amount,
                            date = formattedDate,
                            description = "",
                            sourceId = sourceId,
                            categorieId = categoryId
                        )

                        Log.d("IMPORT", "Importat: ${formattedDate} | ${sourceId} | ${categoryId} | $amount lei")

                        db.transactionDao().insert(transaction)
                    }
                }
            }
        }
}
