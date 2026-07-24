package nz.co.bealetimesheet.export

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import nz.co.bealetimesheet.R
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TimesheetPdfExporter {

    fun createBlankTemplatePdf(
        context: Context,
        employeeName: String = "",
        weekStarting: String = ""
    ): File {
        val templateBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.beale_timesheet_template
        )

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(
            templateBitmap.width,
            templateBitmap.height,
            1
        ).create()

        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(
            templateBitmap,
            0f,
            0f,
            imagePaint
        )

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(25, 55, 180)
            textSize = templateBitmap.width * 0.023f
            typeface = Typeface.create(
                "cursive",
                Typeface.BOLD
            )
        }

        if (employeeName.isNotBlank()) {
            canvas.drawText(
                employeeName,
                templateBitmap.width * 0.235f,
                templateBitmap.height * 0.090f,
                textPaint
            )
        }

        if (weekStarting.isNotBlank()) {
            val formattedWeekStarting = formatDate(
                weekStarting
            )

            canvas.drawText(
                formattedWeekStarting,
                templateBitmap.width * 0.680f,
                templateBitmap.height * 0.090f,
                textPaint
            )
        }

        pdfDocument.finishPage(page)

        val outputFile = File(
            context.cacheDir,
            "beale_timesheet_preview.pdf"
        )

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()
        templateBitmap.recycle()

        return outputFile
    }

    private fun formatDate(
        dateText: String
    ): String {
        return runCatching {
            LocalDate.parse(dateText).format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
        }.getOrElse {
            dateText
        }
    }
}