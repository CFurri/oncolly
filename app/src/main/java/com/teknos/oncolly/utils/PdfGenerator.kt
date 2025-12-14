package com.teknos.oncolly.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateAndSharePatientPdf(
        context: Context,
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        try {
            // 1. Setup
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size approx (72 dpi)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val width = pageInfo.pageWidth
            val height = pageInfo.pageHeight

            // Colors
            val primaryColor = Color.parseColor("#259DF4")
            val darkColor = Color.parseColor("#2C3E50")
            val greyColor = Color.parseColor("#7F8C8D")

            val titlePaint = Paint().apply {
                color = primaryColor
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val labelPaint = Paint().apply {
                color = greyColor
                textSize = 14f
                textAlign = Paint.Align.LEFT
            }

            val valuePaint = Paint().apply {
                color = darkColor
                textSize = 16f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }

            // 2. Header
            canvas.drawText("Accés Pacient Oncolly", width / 2f, 80f, titlePaint)
            
            // Line
            val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 2f }
            canvas.drawLine(50f, 100f, width - 50f, 100f, linePaint)

            // 3. Details
            var y = 150f
            val startX = 60f
            
            fun drawField(label: String, value: String) {
                canvas.drawText(label, startX, y, labelPaint)
                canvas.drawText(value, startX, y + 25f, valuePaint)
                y += 60f
            }

            drawField("NOM:", "$firstName $lastName")
            drawField("EMAIL:", email)
            drawField("CONTRASENYA:", password)

            // 4. QR Code
            y += 40f
            canvas.drawText("Escaneja per iniciar sessió", width / 2f, y, labelPaint.apply { textAlign = Paint.Align.CENTER })
            y += 20f

            val deepLink = "oncolly://login?e=$email&p=$password"
            val qrBitmap = generateQrCode(deepLink)

            if (qrBitmap != null) {
                val qrSize = 250
                val qrX = (width - qrSize) / 2
                val dstRect = Rect(qrX, y.toInt(), qrX + qrSize, y.toInt() + qrSize)
                canvas.drawBitmap(qrBitmap, null, dstRect, null)
            }

            // Footer
            val footerPaint = Paint().apply { 
                color = greyColor 
                textSize = 10f 
                textAlign = Paint.Align.CENTER 
            }
            canvas.drawText("Generat automàticament per Oncolly", width / 2f, height - 50f, footerPaint)

            pdfDocument.finishPage(page)

            // 5. Save
            val fileName = getFileName(firstName, lastName)
            val file = File(context.filesDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            // 6. Share
            sharePdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper for consistency
    fun getFileName(first: String, last: String) = "Oncolly_${first.replace(" ", "")}_${last.replace(" ", "")}.pdf"

    fun shareExistingPdf(context: Context, firstName: String, lastName: String): Boolean {
        val fileName = getFileName(firstName, lastName)
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            sharePdf(context, file)
            return true
        }
        return false
    }

    private fun generateQrCode(text: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val chooser = Intent.createChooser(intent, "Compartir Accés Pacient")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}