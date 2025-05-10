package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddSourceActivity : AppCompatActivity() {

    private lateinit var sourceDao: SourceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_source)

        val sourceNameInput = findViewById<EditText>(R.id.editSourceName)
        val sourceTypeSpinner = findViewById<Spinner>(R.id.spinnerSourceType)
        val initialAmountInput = findViewById<EditText>(R.id.editInitialAmount)
        val saveButton = findViewById<Button>(R.id.buttonSaveSource)

        // Tipuri de surse disponibile
        val tipuri = listOf("Numerar", "Card", "Cont Curent")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipuri
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceTypeSpinner.adapter = spinnerAdapter

        // DAO din baza de date
        val db = DatabaseProvider.getDatabase(this)
        sourceDao = db.sourceDao()

        saveButton.setOnClickListener {
            val name = sourceNameInput.text.toString().trim()
            val type = sourceTypeSpinner.selectedItem?.toString()?.trim()
            val amount = initialAmountInput.text.toString().toDoubleOrNull()

            if (name.isEmpty() || type.isNullOrEmpty() || amount == null) {
                Toast.makeText(this, "Completează toate câmpurile corect", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newSource = Source(
                name = name,
                type = type,
                initialAmount = amount
            )

            lifecycleScope.launch {
                sourceDao.insert(newSource)
                runOnUiThread {
                    Toast.makeText(this@AddSourceActivity, "Sursă salvată", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
