package com.triplogger.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.triplogger.data.TripEntity
import com.triplogger.data.TripStatistics
import java.io.File
import java.io.FileOutputStream

class PdfExporter(private val context: Context) {
    fun exportTrips(trips: List<TripEntity>, statistics: TripStatistics?): Uri? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        
        val headerPaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.DKGRAY
        }
        
        val textPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }
        
        val linePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }
        
        var y = 40
        
        canvas.drawText("Отчет о поездках", 40f, y.toFloat(), titlePaint)
        y += 30
        
        statistics?.let {
            canvas.drawText("Статистика:", 40f, y.toFloat(), headerPaint)
            y += 20
            canvas.drawText("Количество поездок: ${it.tripCount}", 50f, y.toFloat(), textPaint)
            y += 20
            canvas.drawText("Общий пробег: ${String.format("%.2f", it.totalDistance)} км", 50f, y.toFloat(), textPaint)
            y += 20
            canvas.drawText("Общий расход: ${String.format("%.2f", it.totalFuel)} л", 50f, y.toFloat(), textPaint)
            y += 20
            canvas.drawText("Средний расход: ${String.format("%.2f", it.averageConsumption)} л/100км", 50f, y.toFloat(), textPaint)
            y += 30
        }
        
        val startX = 40
        var currentX = startX
        val rowHeight = 25
        
        val headers = arrayOf("Дата", "Пробег (км)", "Расход (л)", "Норма")
        val colWidths = arrayOf(120, 150, 120, 150)
        
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, currentX.toFloat(), y.toFloat(), headerPaint)
            currentX += colWidths[index]
        }
        y += 5
        canvas.drawLine(startX.toFloat(), y.toFloat(), (startX + colWidths.sum()).toFloat(), y.toFloat(), linePaint)
        y += rowHeight
        
        trips.forEach { trip ->
            currentX = startX
            val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm
            
            canvas.drawText(trip.date, currentX.toFloat(), y.toFloat(), textPaint)
            currentX += colWidths[0]
            
            canvas.drawText(String.format("%.2f", totalDistance), currentX.toFloat(), y.toFloat(), textPaint)
            currentX += colWidths[1]
            
            canvas.drawText(String.format("%.2f", trip.fuelConsumed), currentX.toFloat(), y.toFloat(), textPaint)
            currentX += colWidths[2]
            
            canvas.drawText(String.format("%.2f", trip.seasonNorm), currentX.toFloat(), y.toFloat(), textPaint)
            y += rowHeight
            
            if (y > 800) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
                val newPage = document.startPage(newPageInfo)
                y = 40
            }
        }
        
        document.finishPage(page)
        
        val fileName = "trips_report_${System.currentTimeMillis()}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
        document.writeTo(FileOutputStream(file))
        document.close()
        
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
