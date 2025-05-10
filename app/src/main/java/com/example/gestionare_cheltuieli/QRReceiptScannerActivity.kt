package com.example.gestionare_cheltuieli

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRReceiptScannerActivity : AppCompatActivity(){
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var scanAgainBtn: Button
    private var hasScanned = false

    val db = DatabaseProvider.getDatabase(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrreceiptscanner)



        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            startCamera()
        }

        scanAgainBtn = findViewById(R.id.scanAgainBtn)

        scanAgainBtn.setOnClickListener {
            hasScanned = false
            Toast.makeText(this, "Poți scana un nou bon.", Toast.LENGTH_SHORT).show()
            scanAgainBtn.visibility = View.GONE
        }

    }
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()


            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (!hasScanned) {
                                            barcode.rawValue?.let {
                                                hasScanned = true  // ⚠️ Setăm flag-ul pentru a opri scanările ulterioare
                                                extractDataFromQR(it)
                                                Log.d("QR", "Cod scanat: $it")
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("QR", "Eroare scanare", it)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraX", "Bind failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Permisiune cameră necesară!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun extractDataFromQR(qrData: String) {
        if (qrData.startsWith("http")) {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(qrData).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    Log.e("HTTP", "Eroare cerere QR link", e)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val html = response.body?.string() ?: return
                    Log.d("HTTP_RESPONSE", html)
                    val doc = Jsoup.parse(html)

                    // --- Furnizor ---
                    val name = doc.select("p.text-gray-600.text-xs")
                        .firstOrNull { it.text().contains("S.R.L.") }
                        ?.text()?.trim() ?: "Necunoscut"

                    val fiscalCode = doc.select("p.text-gray-600.text-xs")
                        .firstOrNull { it.text().contains("COD FISCAL") }
                        ?.text()?.replace("COD FISCAL:", "")?.trim() ?: "N/A"

                    val address = doc.select("p.text-gray-600.text-xs")
                        .firstOrNull { it.text().contains("bd.") || it.text().contains("Chisinau") }
                        ?.text()?.trim() ?: "N/A"

                    // --- Bon ---
                    val rawDate = doc.select("span.text-base")
                        .firstOrNull { it.text().contains("DATA") }
                        ?.text()?.replace("DATA", "")?.trim() ?: "N/A"

                    // Transformă "08.05.2025" în "2025-05-08"
                    val date = try {
                        val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val parsedDate = inputFormat.parse(rawDate)
                        outputFormat.format(parsedDate!!)
                    } catch (e: Exception) {
                        Log.e("DATE_PARSE", "Eroare la conversia datei: $rawDate", e)
                        rawDate // fallback: trimite data brută
                    }


                    val time = doc.select("span.font-medium")
                        .firstOrNull { it.text().contains(":") }
                        ?.text()?.trim() ?: "N/A"

                    val receiptNumber = doc.select("span.font-medium")
                        .firstOrNull { it.text().contains("№") }
                        ?.text()?.replace("№:", "")?.trim() ?: "Fără număr"

                    val paymentMethod = doc.select("span.text-base")
                        .firstOrNull { it.text().contains("CARD") || it.text().contains("NUMERAR") }
                        ?.text()?.trim() ?: "N/A"

                    val total = doc.select("span.text-base")
                        .firstOrNull { it.text().trim().equals("TOTAL", ignoreCase = true) }
                        ?.nextElementSibling()
                        ?.text()?.replace(",", ".")
                        ?.toDoubleOrNull() ?: 0.0

                    // --- Produse ---
                    // --- Produse (doar între blocuri cu ```` delimitatoare) ---

                    val receiptItems = mutableListOf<ReceiptItem>()
                    val allDivs = doc.select("div.flex.justify-between.items-center")

                    var i = 0
                    while (i < allDivs.size - 1) {
                        val nameAndQtyDiv = allDivs[i]
                        val totalPriceDiv = allDivs[i + 1]

                        val nameSpan = nameAndQtyDiv.select("span").getOrNull(0)?.text()?.trim().orEmpty()
                        val qtyPriceSpan = nameAndQtyDiv.select("span").getOrNull(1)?.text()?.trim().orEmpty()
                        val totalPriceSpan = totalPriceDiv.select("span").getOrNull(1)?.text()?.trim().orEmpty()

                        val isValidTotal = totalPriceSpan.matches(Regex("""\d+[.,]\d{2}\s*[A-Z]?"""))
                        val qtyPriceMatch = Regex("""(\d+[.,]?\d*)\s*[xX]\s*(\d+[.,]?\d*)""").find(qtyPriceSpan)

                        if (nameSpan.isNotEmpty() && isValidTotal && qtyPriceMatch != null) {
                            val quantity = qtyPriceMatch.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
                            val unitPrice = qtyPriceMatch.groupValues[2].replace(",", ".").toDoubleOrNull() ?: 0.0
                            val cost = quantity * unitPrice
                            val unit = "buc"  // opțional

                            val item = ReceiptItem(
                                receiptId = 0, // va fi setat după inserarea bonului
                                productName = nameSpan,
                                unit = unit,
                                quantity = quantity,
                                unitPrice = unitPrice,
                                cost = cost
                            )

                            Log.d("ITEM_EXTRAS", "$nameSpan – $quantity x $unitPrice = $cost")
                            receiptItems.add(item)
                        }

                        i += 2
                    }



                    // --- Salvare în baza de date ---
                    runOnUiThread {
                        scanAgainBtn.visibility = View.VISIBLE
                        lifecycleScope.launch {
                            val db = DatabaseProvider.getDatabase(this@QRReceiptScannerActivity)

                            // 1. Găsește sau inserează furnizorul
                            val existingSupplier = db.supplierDao().getByNameAndFiscalCode(name, fiscalCode)
                            val supplierId = existingSupplier?.id ?: db.supplierDao().insert(
                                Supplier(name = name, address = address, fiscalCode = fiscalCode)
                            ).toInt()

                            // 2. Inserează bonul
                            val receipt = Receipt(
                                supplierId = supplierId,
                                receiptNumber = receiptNumber,
                                date = date,
                                time = time,
                                paymentMethod = paymentMethod,
                                total = total
                            )

                            val receiptId = db.receiptDao().insert(receipt).toInt()

                            // 3. Inserează produsele
                            val finalItems = receiptItems.map { it.copy(receiptId = receiptId) }
                            db.receiptItemDao().insertAll(finalItems)

                            Toast.makeText(applicationContext, "Bon salvat!", Toast.LENGTH_SHORT).show()

                            // 4. Trimite către NewRecordActivity
                            val intent = Intent(this@QRReceiptScannerActivity, NewRecordActivity::class.java).apply {
                                putExtra("total", total)
                                putExtra("date", date)
                                putExtra("category", "produse alimentare")
                                putExtra("subcategory", "supermarket")
                            }
                            startActivity(intent)
                        }
                    }

                }
            })
        } else {
            Log.w("QR_PARSE", "QR-ul nu conține un link. Nu s-a procesat nimic.")
        }
    }
}