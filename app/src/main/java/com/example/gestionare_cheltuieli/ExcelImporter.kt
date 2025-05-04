package com.example.gestionare_cheltuieli

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

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

        suspend fun importTransactions(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
            val file = File(context.filesDir,"Tranzactii.txt")
            if (!file.exists()) return@withContext

            file.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split("|")
                    if (parts.size >= 5) {
                        val amount = parts[0].trim().toDoubleOrNull() ?: return@forEach
                        val type = parts[1].trim()
                        val category = parts[2].trim()
                        val date = parts[3].trim()
                        val sourceId = parts[4].trim().toIntOrNull() ?: return@forEach

                        val transaction = Transaction(
                            amount = amount,
                            type = type,
                            category = category,
                            date = date,
                            description = "",
                            sourceId = sourceId
                        )

                       //Log.d("inregistrare", "I| ${date} - ${type.uppercase()} ${amount} lei | Cat=${category} | SourceId=${sourceId}")

                        db.transactionDao().insert(transaction)
                    }
                }
            }
        }
}
