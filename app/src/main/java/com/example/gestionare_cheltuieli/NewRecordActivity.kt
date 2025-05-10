package com.example.gestionare_cheltuieli

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewRecordActivity : AppCompatActivity() {
    private lateinit var categorieDao: CategorieDao
    private lateinit var sourceDao: SourceDao
    private lateinit var transactionDao: TransactionDao

    private lateinit var categorii: List<Categorie>
    private lateinit var sources: List<Source>
    private lateinit var categoriiDistincte: List<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_record)

        val incomeInput = findViewById<TextInputEditText>(R.id.incomeInput)
        val expenseInput = findViewById<TextInputEditText>(R.id.expenseInput)
        val button5 = findViewById<Button>(R.id.button5)
        val categorySpinner = findViewById<Spinner>(R.id.textCategory)
        val subCategorySpinner = findViewById<Spinner>(R.id.textSubcategory)
        val sourceSpinner = findViewById<Spinner>(R.id.textSource)
        val dateInput = findViewById<TextInputEditText>(R.id.dateInputEditTex)



        val db = DatabaseProvider.getDatabase(this)
        categorieDao = db.categorieDao()
        sourceDao = db.sourceDao()
        transactionDao = db.transactionDao()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setText(dateFormat.format(Date()))

        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
                    dateInput.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        lifecycleScope.launch {
            categorii = categorieDao.getAll()
            sources = sourceDao.getAll()

            categoriiDistincte = categorii.map { it.category }.distinct()

            val categoryAdapter = ArrayAdapter(
                this@NewRecordActivity,
                android.R.layout.simple_spinner_item,
                categoriiDistincte
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = categoryAdapter

            val sumaScanata = intent.getDoubleExtra("total", -1.0)
            val dataScanata = intent.getStringExtra("date")

            val categorieSupermarket = categorii.find {
                it.category.equals("produse alimentare", ignoreCase = true) &&
                        it.subcategory.equals("supermarket", ignoreCase = true)
            }

            if (sumaScanata > 0) {
                expenseInput.setText(sumaScanata.toString())
                incomeInput.setText("") // dezactivează câmpul venit
                expenseInput.isEnabled = true
                incomeInput.isEnabled = false
            }

            if (!dataScanata.isNullOrEmpty()) {
                dateInput.setText(dataScanata)
            }

            val indexCat = categoriiDistincte.indexOf(categorieSupermarket?.category)
            if (indexCat >= 0) categorySpinner.setSelection(indexCat)

            subCategorySpinner.post {
                val subcategorii = categorii.filter { it.category == categorieSupermarket?.category }
                val indexSub = subcategorii.map { it.subcategory }.indexOf(categorieSupermarket?.subcategory)
                if (indexSub >= 0) subCategorySpinner.setSelection(indexSub)
            }

            val sourceAdapter = ArrayAdapter(
                this@NewRecordActivity,
                android.R.layout.simple_spinner_item,
                sources.map { it.name }
            )
            sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sourceSpinner.adapter = sourceAdapter
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categoriiDistincte[position]
                val filteredSubcategories = categorii
                    .filter { it.category == selectedCategory }
                    .map { it.subcategory }
                    .distinct()

                val subAdapter = ArrayAdapter(
                    this@NewRecordActivity,
                    android.R.layout.simple_spinner_item,
                    filteredSubcategories
                )
                subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                subCategorySpinner.adapter = subAdapter

                val types = categorii
                    .filter { it.category == selectedCategory }
                    .map { it.type.lowercase() }

                incomeInput.isEnabled = types.contains("venit")
                expenseInput.isEnabled = types.contains("cheltuiala")

                if (!incomeInput.isEnabled) incomeInput.setText("")
                if (!expenseInput.isEnabled) expenseInput.setText("")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }






        button5.setOnClickListener {
            val selectedCategory = categorySpinner.selectedItem?.toString()
            val selectedSubcategory = subCategorySpinner.selectedItem?.toString()
            val selectedSourceName = sourceSpinner.selectedItem?.toString()
            val income = incomeInput.text.toString().toDoubleOrNull()
            val expense = expenseInput.text.toString().toDoubleOrNull()
            val selectedDate = dateInput.text.toString()

            if (selectedCategory.isNullOrBlank() || selectedSubcategory.isNullOrBlank()) {
                Toast.makeText(this, "Selectează categoria și subcategoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (income != null && expense != null) {
                Toast.makeText(this, "Completează doar un câmp (venit sau cheltuială)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expectedType = when {
                income != null -> "venit"
                expense != null -> "cheltuiala"
                else -> {
                    Toast.makeText(this, "Introdu o sumă validă", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val selectedCategorie = categorii.find {
                it.category == selectedCategory &&
                        it.subcategory == selectedSubcategory &&
                        it.type.lowercase() == expectedType
            }

            if (selectedCategorie == null) {
                Toast.makeText(this, "Categoria selectată nu este validă pentru $expectedType", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedSource = sources.find { it.name == selectedSourceName }
            if (selectedSource == null) {
                Toast.makeText(this, "Selectează o sursă validă", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = income ?: expense ?: 0.0

            val newTransaction = Transaction(
                amount = amount,
                description = "",
                date = selectedDate,
                categorieId = selectedCategorie.id,
                sourceId = selectedSource.id
            )

            lifecycleScope.launch {
                transactionDao.insert(newTransaction)
                runOnUiThread {
                    Toast.makeText(this@NewRecordActivity, "Tranzacție salvată", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
