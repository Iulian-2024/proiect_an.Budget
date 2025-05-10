package com.example.gestionare_cheltuieli

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class DeleteSourceActivity : AppCompatActivity() {

    private lateinit var sourceDao: SourceDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SelectableSourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_source)

        val db = DatabaseProvider.getDatabase(this)
        sourceDao = db.sourceDao()

        recyclerView = findViewById(R.id.recyclerViewSources)
        val buttonDelete = findViewById<Button>(R.id.buttonDelete)

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val sources = sourceDao.getAll()
            adapter = SelectableSourceAdapter(sources)
            runOnUiThread {
                recyclerView.adapter = adapter
            }
        }

        buttonDelete.setOnClickListener {
            val selectedIds = adapter.getSelectedSourceIds()

            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Selectează cel puțin o sursă", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                selectedIds.forEach { id ->
                    sourceDao.deleteById(id)
                }
                runOnUiThread {
                    Toast.makeText(this@DeleteSourceActivity, "Sursele au fost șterse", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
