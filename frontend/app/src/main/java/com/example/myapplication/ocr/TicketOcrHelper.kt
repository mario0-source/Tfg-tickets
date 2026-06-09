package com.example.myapplication.ocr

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

object TicketOcrHelper {

    fun createTempImageUri(context: Context): Uri {
        val file = File(context.cacheDir, "ticket_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun processImage(
        context: Context,
        uri: Uri,
        onSuccess: (ParsedReceipt) -> Unit,
        onFailure: (() -> Unit)? = null
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val orderedLines = extractOrderedLines(visionText)
                    onSuccess(ReceiptOcrParser.parseFromLines(orderedLines))
                }
                .addOnFailureListener { onFailure?.invoke() }
        } catch (_: Exception) {
            onFailure?.invoke()
        }
    }

    /**
     * Ordena las líneas de arriba a abajo (como en el ticket), no en el orden aleatorio del OCR.
     */
    private fun extractOrderedLines(visionText: Text): List<String> {
        return visionText.textBlocks
            .flatMap { block ->
                block.lines.map { line ->
                    val box = line.boundingBox
                    Triple(box?.top ?: 0, box?.left ?: 0, line.text.trim())
                }
            }
            .sortedWith(compareBy({ it.first }, { it.second }))
            .map { it.third }
            .map { it.replace(Regex("""\s+"""), " ").trim() }
            .filter { it.isNotBlank() }
    }
}
