package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.myapplication.model.TicketDto
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketPdfExporter {

    fun export(context: Context, ticket: TicketDto): Intent? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = 14f }

        var y = 48f
        canvas.drawText("Digital Nebula — Ticket", 40f, y, titlePaint)
        y += 32f
        canvas.drawText("Tienda: ${ticket.nombre}", 40f, y, bodyPaint)
        y += 22f
        canvas.drawText("Fecha: ${ticket.fecha}", 40f, y, bodyPaint)
        y += 22f
        canvas.drawText("Categoría: ${ticket.categoria}", 40f, y, bodyPaint)
        y += 22f
        canvas.drawText("Total: %.2f€".format(ticket.precio), 40f, y, bodyPaint)
        y += 32f
        canvas.drawText("Productos:", 40f, y, titlePaint)
        y += 24f

        ticket.productos.forEach { product ->
            canvas.drawText(
                "• ${product.nombre} — %.2f€".format(product.precio ?: 0.0),
                48f,
                y,
                bodyPaint
            )
            y += 20f
        }

        document.finishPage(page)

        val fileName = "ticket_${ticket.id}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
