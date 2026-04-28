package com.triplogger.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.triplogger.data.TripEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter(private val context: Context) {
    fun exportTrips(trips: List<TripEntity>): Boolean {
        return try {
            val fileName = "trips_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, fileName)
            val writer = file.bufferedWriter(charset = Charsets.UTF_8)

            writer.use { out ->
                // BOM для Excel
                out.write('\uFEFF')

                // Заголовки
                out.write("Дата;Время выезда;Время возвращения;Начало;Конец;")
                out.write("Одометр начало;Одометр конец;Пробег GPS (км);Пробег ручной (км);")
                out.write("Пробег итог (км);Расход (л);Норма (л/100км);Комментарий")
                out.newLine()

                // Данные — сортировка от ранних к поздним
                val sortedTrips = trips.sortedWith(
                    compareBy<TripEntity> { it.date }.thenBy { it.departureTime }
                )

                sortedTrips.forEach { trip ->
                    val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm

                    out.write("${trip.date};${trip.departureTime};${trip.arrivalTime};")
                    out.write("${trip.startPoint};${trip.endPoint};")
                    out.write("${trip.odometerStart};${trip.odometerEnd};")
                    out.write("${String.format("%.2f", trip.gpsDistanceKm)};")
                    out.write("${String.format("%.0f", trip.manualDistanceKm)};")
                    out.write("${String.format("%.0f", totalDistance)};")
                    out.write("${String.format("%.2f", trip.fuelConsumed)};")
                    out.write("${String.format("%.2f", trip.seasonNorm)};")
                    out.write(trip.comment.replace(";", ","))
                    out.newLine()
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка экспорта: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }
}
