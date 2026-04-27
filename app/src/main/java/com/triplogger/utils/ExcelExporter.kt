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
            FileOutputStream(file).use { fos ->
                fos.write(0xEF)
                fos.write(0xBB)
                fos.write(0xBF)

                val headers = "Дата;Время выезда;Время возвращения;Начало;Конец;" +
                             "Одометр начало;Одометр конец;Пробег GPS (км);Пробег ручной (км);" +
                             "Пробег итог (км);Расход (л);Норма (л/100км);Комментарий\\n"
                fos.write(headers.toByteArray())

                trips.forEach { trip ->
                    val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm
                    val line = "${trip.date};${trip.departureTime};${trip.arrivalTime};" +
                              "${trip.startPoint};${trip.endPoint};" +
                              "${trip.odometerStart};${trip.odometerEnd};" +
                              "${String.format("%.2f", trip.gpsDistanceKm)};" +
                              "${String.format("%.0f", trip.manualDistanceKm)};" +
                              "${String.format("%.0f", totalDistance)};" +
                              "${String.format("%.2f", trip.fuelConsumed)};" +
                              "${String.format("%.2f", trip.seasonNorm)};" +
                              "${trip.comment.replace(";", ",")}\\n"
                    fos.write(line.toByteArray())
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
