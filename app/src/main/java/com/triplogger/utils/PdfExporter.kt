package com.triplogger.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.triplogger.data.TripEntity
import com.triplogger.data.TripStatistics
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {
    fun exportTrips(trips: List<TripEntity>, statistics: TripStatistics?): Boolean {
        return try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            
            // Первая страница - статистика
            val page1 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
            var canvas = page1.canvas
            
            // Фон
            canvas.drawColor(Color.WHITE)
            
            val titlePaint = Paint().apply {
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                color = Color.rgb(33, 150, 243)
                isAntiAlias = true
            }
            
            val headerPaint = Paint().apply {
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                color = Color.DKGRAY
                isAntiAlias = true
            }
            
            val textPaint = Paint().apply {
                textSize = 14f
                color = Color.BLACK
                isAntiAlias = true
            }
            
            val linePaint = Paint().apply {
                color = Color.rgb(33, 150, 243)
                strokeWidth = 2f
            }
            
            var y = 50f
            
            // Заголовок
            canvas.drawText("Отчет о поездках", 40f, y, titlePaint)
            y += 40
            canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
            y += 30
            
            // Статистика
            statistics?.let {
                canvas.drawText("Статистика периода", 40f, y, headerPaint)
                y += 30
                
                val stats = arrayOf(
                    "Количество поездок: ${it.tripCount}",
                    "Общий пробег: ${String.format("%.2f", it.totalDistance)} км",
                    "Общий расход топлива: ${String.format("%.2f", it.totalFuel)} л",
                    "Средний расход: ${String.format("%.2f", it.averageConsumption)} л/100км"
                )
                
                stats.forEach { stat ->
                    canvas.drawText(stat, 60f, y, textPaint)
                    y += 25
                }
            }
            
            document.finishPage(page1)
            
            // Вторая страница - таблица поездок
            val page2 = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create())
            canvas = page2.canvas
            canvas.drawColor(Color.WHITE)
            
            y = 40f
            canvas.drawText("Список поездок", 40f, y, titlePaint)
            y += 30
            canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
            y += 20
            
            // Заголовки таблицы
            val colWidths = arrayOf(100f, 100f, 120f, 120f, 100f)
            val colHeaders = arrayOf("Дата", "Маршрут", "Пробег (км)", "Расход (л)", "Норма")
            var x = 40f
            
            canvas.drawRect(x, y - 15, pageWidth - 40f, y + 10, Paint().apply {
                color = Color.rgb(33, 150, 243)
                style = Paint.Style.FILL
            })
            
            colHeaders.forEachIndexed { index, header ->
                canvas.drawText(header, x + 5, y, Paint().apply {
                    textSize = 12f
                    color = Color.WHITE
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                })
                x += colWidths[index]
            }
            y += 25
            
            // Данные
            val dataPaint = Paint().apply {
                textSize = 11f
                color = Color.BLACK
                isAntiAlias = true
            }
            
            trips.forEach { trip ->
                if (y > pageHeight - 50) {
                    document.finishPage(page2)
                    val newPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create())
                    canvas = newPage.canvas
                    canvas.drawColor(Color.WHITE)
                    y = 40f
                }
                
                x = 40f
                val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm
                
                canvas.drawText(trip.date, x + 5, y, dataPaint)
                x += colWidths[0]
                
                val route = "${trip.startPoint.take(12)}→${trip.endPoint.take(12)}"
                canvas.drawText(route, x + 5, y, dataPaint)
                x += colWidths[1]
                
                canvas.drawText(String.format("%.2f", totalDistance), x + 5, y, dataPaint)
                x += colWidths[2]
                
                canvas.drawText(String.format("%.2f", trip.fuelConsumed), x + 5, y, dataPaint)
                x += colWidths[3]
                
                canvas.drawText(String.format("%.2f", trip.seasonNorm), x + 5, y, dataPaint)
                y += 20
            }
            
            document.finishPage(page2)
            
            // Сохраняем файл
            val fileName = "trips_report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.pdf"
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!dir.exists()) dir.mkdirs()
            
            val file = File(dir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            
            // Открываем файл
            Toast.makeText(context, "PDF сохранён в: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка создания PDF: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
