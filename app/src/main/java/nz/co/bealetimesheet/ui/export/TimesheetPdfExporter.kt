package nz.co.bealetimesheet.export

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import nz.co.bealetimesheet.R
import java.io.File
import java.io.FileOutputStream

object TimesheetPdfExporter {

    fun createBlankTemplatePdf(
        context: Context
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(
            templateBitmap,
            0f,
            0f,
            paint
        )

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
}