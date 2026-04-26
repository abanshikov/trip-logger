package com.triplogger.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.triplogger.data.TripEntity
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelExporter(private val context: Context) {
    fun exportTrips(trips: List<TripEntity>): Uri? {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Поездки")
        
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        
        val dataStyle = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        
        val headers = arrayOf("Дата", "Начало", "Конец", "Одометр начало", "Одометр конец", 
                             "Пробег GPS (км)", "Пробег ручной (км)", "Пробег итог (км)", 
                             "Расход (л)", "Норма (л/100км)", "Комментарий")
        
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }
        
        trips.forEachIndexed { index, trip ->
            val row = sheet.createRow(index + 1)
            val totalDistance = if (trip.usedGpsDistance) trip.gpsDistanceKm else trip.manualDistanceKm
            
            row.createCell(0).apply { setCellValue(trip.date); cellStyle = dataStyle }
            row.createCell(1).apply { setCellValue(trip.startPoint); cellStyle = dataStyle }
            row.createCell(2).apply { setCellValue(trip.endPoint); cellStyle = dataStyle }
            row.createCell(3).apply { setCellValue(trip.odometerStart); cellStyle = dataStyle }
            row.createCell(4).apply { setCellValue(trip.odometerEnd); cellStyle = dataStyle }
            row.createCell(5).apply { setCellValue(trip.gpsDistanceKm); cellStyle = dataStyle }
            row.createCell(6).apply { setCellValue(trip.manualDistanceKm); cellStyle = dataStyle }
            row.createCell(7).apply { setCellValue(totalDistance); cellStyle = dataStyle }
            row.createCell(8).apply { setCellValue(trip.fuelConsumed); cellStyle = dataStyle }
            row.createCell(9).apply { setCellValue(trip.seasonNorm); cellStyle = dataStyle }
            row.createCell(10).apply { setCellValue(trip.comment); cellStyle = dataStyle }
        }
        
        (0..10).forEach { sheet.autoSizeColumn(it) }
        
        val fileName = "trips_export_${System.currentTimeMillis()}.xlsx"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
