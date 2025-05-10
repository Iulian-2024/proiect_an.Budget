package com.example.gestionare_cheltuieli

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var dao: TransactionDao
    private lateinit var sourceDao: SourceDao
    private var transactionId: Int = -1
    private lateinit var transaction: Transaction
    private lateinit var sources: List<Source>
    private lateinit var categorieDao: CategorieDao
    private lateinit var categorii: List<Categorie>
    private var subcategorieInitiala: String? = null

    lateinit var categoriiDistincte: List<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val incomeInput = findViewById<TextInputEditText>(R.id.incomeInput)
        val expenseInput = findViewById<TextInputEditText>(R.id.expenseInput)
        val dateInput = findViewById<TextInputEditText>(R.id.dateInputEditTex)
        val categorySpinner = findViewById<Spinner>(R.id.textCategory)
        val subCategorySpinner = findViewById<Spinner>(R.id.textSubcategory)
        val sourceSpinner = findViewById<Spinner>(R.id.textSource)
        val button = findViewById<Button>(R.id.button5)

        val db = DatabaseProvider.getDatabase(this)

        dao = db.transactionDao()
        sourceDao = db.sourceDao()
        categorieDao = db.categorieDao()

        transactionId = intent.getIntExtra("transaction_id", -1)

                // 1. Obținem categoriile din baza de date și setăm spinnerul pentru categorii mari
        lifecycleScope.launch {
            categorii = categorieDao.getAll()
            sources = sourceDao.getAll()
            categoriiDistincte = categorii.map { it.category }.distinct()

            val categoryAdapter = ArrayAdapter(
                this@EditTransactionActivity,
                android.R.layout.simple_spinner_item,
                categoriiDistincte
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = categoryAdapter

            // 2. După ce avem categoriile, putem obține tranzacția și popula UI-ul
            transaction = dao.getById(transactionId) ?: return@launch
            val cat = categorii.find { it.id == transaction.categorieId }

            runOnUiThread {
                dateInput.setText(transaction.date)

                // Setăm sursa
                val sourceNames = sources.map { it.name }
                val sourceAdapter = ArrayAdapter(
                    this@EditTransactionActivity,
                    android.R.layout.simple_spinner_item,
                    sourceNames
                )
                sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sourceSpinner.adapter = sourceAdapter

                val sourceIndex = sources.indexOfFirst { it.id == transaction.sourceId }
                if (sourceIndex >= 0) {
                    sourceSpinner.setSelection(sourceIndex)
                }

                // Setăm categoria și subcategoria
                subcategorieInitiala = cat?.subcategory
                val categoryIndex = categoriiDistincte.indexOf(cat?.category)
                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex)

                    subCategorySpinner.post {
                        val subcategorii = categorii
                            .filter { it.category == cat?.category && it.type == cat?.type }
                            .map { it.subcategory }

                        val subIndex = subcategorii.indexOf(cat?.subcategory)
                        if (subIndex >= 0) {
                            subCategorySpinner.setSelection(subIndex)
                        }
                    }
                }


                // Setăm suma în câmpul corect
                if (cat?.type == "venit") {
                    incomeInput.setText(transaction.amount.toString())
                } else {
                    expenseInput.setText(transaction.amount.toString())
                }
            }

        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val categorieSelectata = categoriiDistincte[position]

                // Obține `type` doar dacă tranzacția a fost deja încărcată

                val tranzactieCurentaCategorie = categorii.find { it.id == transaction.categorieId }
                val tipSelectat = tranzactieCurentaCategorie?.type?.lowercase()

                val subcategoriiFiltrate = categorii
                    .filter { it.category == categorieSelectata && (tipSelectat == null || it.type.lowercase() == tipSelectat) }
                    .map { it.subcategory }

                    .distinct()

                val subAdapter = ArrayAdapter(
                    this@EditTransactionActivity,
                    android.R.layout.simple_spinner_item,
                    subcategoriiFiltrate
                )
                subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                subCategorySpinner.adapter = subAdapter

                if (subcategorieInitiala != null) {
                    val index = subcategoriiFiltrate.indexOf(subcategorieInitiala)
                    if (index >= 0) {
                        subCategorySpinner.setSelection(index)
                    }
                    subcategorieInitiala = null // setăm o singură dată
                }

                // ✅ Dacă este o singură subcategorie, selecteaz-o automat
                if (subcategoriiFiltrate.size == 1) {
                    subCategorySpinner.setSelection(0)
                }

                // ✅ Activează/dezactivează câmpurile de sumă
                val tipuri = categorii
                    .filter { it.category == categorieSelectata }
                    .map { it.type.lowercase().trim() }

                val esteVenit = tipuri.contains("venit")
                val esteCheltuiala = tipuri.contains("cheltuiala")

                incomeInput.isEnabled = esteVenit
                incomeInput.isFocusable = esteVenit
                incomeInput.isClickable = esteVenit

                expenseInput.isEnabled = esteCheltuiala
                expenseInput.isFocusable = esteCheltuiala
                expenseInput.isClickable = esteCheltuiala

                if (!esteVenit) incomeInput.setText("")
                if (!esteCheltuiala) expenseInput.setText("")
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
                    dateInput.setText(formattedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.show()
        }

        button.text = "Salvează"
        button.setOnClickListener {
            val selectedCategoryName = categorySpinner.selectedItem.toString()
            val selectedSubcategoryName = subCategorySpinner.selectedItem.toString()

            val income = incomeInput.text.toString().toDoubleOrNull()
            val expense = expenseInput.text.toString().toDoubleOrNull()

            val expectedType = when {
                income != null && expense == null -> "venit"
                expense != null && income == null -> "cheltuiala"
                else -> {
                    Toast.makeText(this, "Completează doar un câmp", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val selectedCategorie = categorii.find {
                it.category == selectedCategoryName &&
                        it.subcategory == selectedSubcategoryName &&
                        it.type.lowercase() == expectedType
            }




            // Verificare câmpuri completate


            // Verificare categorie selectată
            if (selectedCategorie == null) {
                Toast.makeText(this, "Selectează o categorie validă", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificare compatibilitate tip
            if (selectedCategorie.type.lowercase() != expectedType) {
                Toast.makeText(this, "Categoria selectată nu este de tip $expectedType", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newAmount = income ?: expense ?: 0.0
            val newDate = dateInput.text.toString()

            val selectedSourceName = sourceSpinner.selectedItem.toString()
            val selectedSource = sources.find { it.name == selectedSourceName }

            if (selectedSource == null) {
                Toast.makeText(this, "Sursă invalidă", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizare tranzacție
            val updated = transaction.copy(
                amount = newAmount,
                date = newDate,
                categorieId = selectedCategorie.id,
                sourceId = selectedSource.id
            )

            lifecycleScope.launch {
                dao.updateTransaction(updated)
                runOnUiThread {
                    Toast.makeText(this@EditTransactionActivity, "Modificat cu succes", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun getCategoryIndex(spinner: Spinner, category: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == category) return i
        }
        return 0
    }
}