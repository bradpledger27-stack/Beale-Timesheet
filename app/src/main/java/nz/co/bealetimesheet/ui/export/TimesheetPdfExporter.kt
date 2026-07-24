package nz.co.bealetimesheet.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import nz.co.bealetimesheet.R
import nz.co.bealetimesheet.data.model.ShiftWithBreaks
import nz.co.bealetimesheet.data.model.TimesheetDayWithShifts
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.max

object TimesheetPdfExporter {

    private const val MAX_SHIFTS_PER_DAY = 3

    /*
    * These positions are percentages of the template image.
    * This allows the layout to continue working if the template
    * image dimensions change.
    */
    private const val EMPLOYEE_NAME_X = 0.235f
    private const val EMPLOYEE_NAME_Y = 0.090f

    private const val WEEK_STARTING_X = 0.680f
    private const val WEEK_STARTING_Y = 0.090f

    /*
    * Main timesheet table positions.
    *
    * We will fine-tune these after viewing the first populated PDF.
    */
    private const val FIRST_DAY_Y = 0.218f
    private const val DAY_HEIGHT = 0.0928f
    private const val SHIFT_ROW_HEIGHT = 0.0308f

    private const val DATE_X = 0.088f
    private const val START_TIME_X = 0.245f
    private const val FINISH_TIME_X = 0.345f
    private const val HOURS_X = 0.445f
    private const val COMMENTS_X = 0.545f
    private const val DAILY_TOTAL_X = 0.875f

    private const val WEEKLY_TOTAL_X = 0.875f
    private const val WEEKLY_TOTAL_Y = 0.885f

    fun createBlankTemplatePdf(
        context: Context,
        employeeName: String = "",
        weekStarting: String = "",
        days: List<TimesheetDayWithShifts> = emptyList()
    ): File {
        val templateBitmap = requireNotNull(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.beale_timesheet_template
            )
        ) {
            "Unable to load the Beale timesheet template."
        }

        val pdfDocument = PdfDocument()

        try {
            val pageInfo = PdfDocument.PageInfo.Builder(
                templateBitmap.width,
                templateBitmap.height,
                1
            ).create()

            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawTemplate(
                canvas = canvas,
                templateBitmap = templateBitmap
            )

            val handwritingPaint = createHandwritingPaint(
                templateBitmap = templateBitmap
            )

            val smallHandwritingPaint = createSmallHandwritingPaint(
                templateBitmap = templateBitmap
            )

            drawHeader(
                canvas = canvas,
                templateBitmap = templateBitmap,
                employeeName = employeeName,
                weekStarting = weekStarting,
                textPaint = handwritingPaint
            )

            val weeklyMinutes = drawTimesheetEntries(
                canvas = canvas,
                templateBitmap = templateBitmap,
                days = days,
                textPaint = smallHandwritingPaint
            )

            if (days.isNotEmpty()) {
                drawWeeklyTotal(
                    canvas = canvas,
                    templateBitmap = templateBitmap,
                    weeklyMinutes = weeklyMinutes,
                    textPaint = handwritingPaint
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

            return outputFile
        } finally {
            pdfDocument.close()

            if (!templateBitmap.isRecycled) {
                templateBitmap.recycle()
            }
        }
    }

    private fun drawTemplate(
        canvas: Canvas,
        templateBitmap: Bitmap
    ) {
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(
            templateBitmap,
            0f,
            0f,
            imagePaint
        )
    }

    private fun createHandwritingPaint(
        templateBitmap: Bitmap
    ): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(25, 55, 180)
            textSize = templateBitmap.width * 0.023f
            typeface = Typeface.create(
                "cursive",
                Typeface.BOLD
            )
        }
    }

    private fun createSmallHandwritingPaint(
        templateBitmap: Bitmap
    ): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(25, 55, 180)
            textSize = templateBitmap.width * 0.0175f
            typeface = Typeface.create(
                "cursive",
                Typeface.BOLD
            )
        }
    }

    private fun drawHeader(
        canvas: Canvas,
        templateBitmap: Bitmap,
        employeeName: String,
        weekStarting: String,
        textPaint: Paint
    ) {
        if (employeeName.isNotBlank()) {
            canvas.drawText(
                employeeName,
                templateBitmap.width * EMPLOYEE_NAME_X,
                templateBitmap.height * EMPLOYEE_NAME_Y,
                textPaint
            )
        }

        if (weekStarting.isNotBlank()) {
            canvas.drawText(
                formatFullDate(weekStarting),
                templateBitmap.width * WEEK_STARTING_X,
                templateBitmap.height * WEEK_STARTING_Y,
                textPaint
            )
        }
    }

    private fun drawTimesheetEntries(
        canvas: Canvas,
        templateBitmap: Bitmap,
        days: List<TimesheetDayWithShifts>,
        textPaint: Paint
    ): Long {
        if (days.isEmpty()) {
            return 0L
        }

        val orderedDays = days.sortedBy { dayWithShifts ->
            parseDate(dayWithShifts.day.date) ?: LocalDate.MAX
        }

        var weeklyMinutes = 0L

        orderedDays.take(7).forEachIndexed { dayIndex, dayWithShifts ->
            val dayTopY = FIRST_DAY_Y + (dayIndex * DAY_HEIGHT)

            drawDayDate(
                canvas = canvas,
                templateBitmap = templateBitmap,
                dateText = dayWithShifts.day.date,
                dayTopY = dayTopY,
                textPaint = textPaint
            )

            val orderedShifts = dayWithShifts.shifts
                .sortedBy { shiftWithBreaks ->
                    shiftWithBreaks.shift.shiftNumber
                }
                .take(MAX_SHIFTS_PER_DAY)

            var dailyMinutes = 0L

            orderedShifts.forEachIndexed { shiftIndex, shiftWithBreaks ->
                val shiftMinutes = calculateWorkedMinutes(
                    shiftWithBreaks = shiftWithBreaks
                )

                dailyMinutes += shiftMinutes

                drawShift(
                    canvas = canvas,
                    templateBitmap = templateBitmap,
                    shiftWithBreaks = shiftWithBreaks,
                    shiftMinutes = shiftMinutes,
                    dayTopY = dayTopY,
                    shiftIndex = shiftIndex,
                    textPaint = textPaint
                )
            }

            weeklyMinutes += dailyMinutes

            drawComments(
                canvas = canvas,
                templateBitmap = templateBitmap,
                comments = dayWithShifts.day.comments,
                dayTopY = dayTopY,
                textPaint = textPaint
            )

            if (orderedShifts.isNotEmpty()) {
                drawDailyTotal(
                    canvas = canvas,
                    templateBitmap = templateBitmap,
                    dailyMinutes = dailyMinutes,
                    dayTopY = dayTopY,
                    textPaint = textPaint
                )
            }
        }

        return weeklyMinutes
    }

    private fun drawDayDate(
        canvas: Canvas,
        templateBitmap: Bitmap,
        dateText: String,
        dayTopY: Float,
        textPaint: Paint
    ) {
        val formattedDate = formatShortDate(dateText)

        canvas.drawText(
            formattedDate,
            templateBitmap.width * DATE_X,
            templateBitmap.height * (dayTopY + 0.022f),
            textPaint
        )
    }

    private fun drawShift(
        canvas: Canvas,
        templateBitmap: Bitmap,
        shiftWithBreaks: ShiftWithBreaks,
        shiftMinutes: Long,
        dayTopY: Float,
        shiftIndex: Int,
        textPaint: Paint
    ) {
        val shift = shiftWithBreaks.shift

        val rowY = dayTopY +
                0.022f +
                (shiftIndex * SHIFT_ROW_HEIGHT)

        val startTime = formatTimeForPdf(
            shift.startTime
        )

        val finishTime = formatTimeForPdf(
            shift.finishTime.orEmpty()
        )

        if (startTime.isNotBlank()) {
            canvas.drawText(
                startTime,
                templateBitmap.width * START_TIME_X,
                templateBitmap.height * rowY,
                textPaint
            )
        }

        if (finishTime.isNotBlank()) {
            canvas.drawText(
                finishTime,
                templateBitmap.width * FINISH_TIME_X,
                templateBitmap.height * rowY,
                textPaint
            )
        }

        if (
            shift.startTime.isNotBlank() &&
            !shift.finishTime.isNullOrBlank()
        ) {
            canvas.drawText(
                formatHours(shiftMinutes),
                templateBitmap.width * HOURS_X,
                templateBitmap.height * rowY,
                textPaint
            )
        }
    }

    private fun drawComments(
        canvas: Canvas,
        templateBitmap: Bitmap,
        comments: String,
        dayTopY: Float,
        textPaint: Paint
    ) {
        if (comments.isBlank()) {
            return
        }

        val commentLines = splitCommentIntoLines(
            comments = comments,
            maximumCharactersPerLine = 30,
            maximumLines = 3
        )

        commentLines.forEachIndexed { lineIndex, line ->
            val rowY = dayTopY +
                    0.022f +
                    (lineIndex * SHIFT_ROW_HEIGHT)

            canvas.drawText(
                line,
                templateBitmap.width * COMMENTS_X,
                templateBitmap.height * rowY,
                textPaint
            )
        }
    }

    private fun drawDailyTotal(
        canvas: Canvas,
        templateBitmap: Bitmap,
        dailyMinutes: Long,
        dayTopY: Float,
        textPaint: Paint
    ) {
        val centreRowY = dayTopY + 0.052f

        canvas.drawText(
            formatHours(dailyMinutes),
            templateBitmap.width * DAILY_TOTAL_X,
            templateBitmap.height * centreRowY,
            textPaint
        )
    }

    private fun drawWeeklyTotal(
        canvas: Canvas,
        templateBitmap: Bitmap,
        weeklyMinutes: Long,
        textPaint: Paint
    ) {
        canvas.drawText(
            formatHours(weeklyMinutes),
            templateBitmap.width * WEEKLY_TOTAL_X,
            templateBitmap.height * WEEKLY_TOTAL_Y,
            textPaint
        )
    }

    private fun calculateWorkedMinutes(
        shiftWithBreaks: ShiftWithBreaks
    ): Long {
        val shift = shiftWithBreaks.shift

        val shiftStart = parseTime(
            shift.startTime
        ) ?: return 0L

        val shiftFinish = parseTime(
            shift.finishTime
        ) ?: return 0L

        val totalShiftMinutes = minutesBetween(
            start = shiftStart,
            finish = shiftFinish
        )

        val totalBreakMinutes = shiftWithBreaks.restBreaks.sumOf { restBreak ->
            val breakStart = parseTime(
                restBreak.startTime
            )

            val breakFinish = parseTime(
                restBreak.finishTime
            )

            if (breakStart == null || breakFinish == null) {
                0L
            } else {
                minutesBetween(
                    start = breakStart,
                    finish = breakFinish
                )
            }
        }

        return max(
            0L,
            totalShiftMinutes - totalBreakMinutes
        )
    }

    private fun minutesBetween(
        start: LocalTime,
        finish: LocalTime
    ): Long {
        val startMinutes =
            (start.hour * 60L) + start.minute

        var finishMinutes =
            (finish.hour * 60L) + finish.minute

        /*
        * If the finish time is earlier than the start time,
        * treat the shift or break as crossing midnight.
        */
        if (finishMinutes < startMinutes) {
            finishMinutes += 24L * 60L
        }

        return finishMinutes - startMinutes
    }

    private fun formatHours(
        totalMinutes: Long
    ): String {
        val safeMinutes = max(0L, totalMinutes)

        val hours = safeMinutes / 60.0

        return String.format(
            Locale.US,
            "%.2f",
            hours
        )
    }

    private fun formatFullDate(
        dateText: String
    ): String {
        val date = parseDate(dateText)
            ?: return dateText

        return date.format(
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy",
                Locale.getDefault()
            )
        )
    }

    private fun formatShortDate(
        dateText: String
    ): String {
        val date = parseDate(dateText)
            ?: return dateText

        return date.format(
            DateTimeFormatter.ofPattern(
                "dd/MM",
                Locale.getDefault()
            )
        )
    }

    private fun formatTimeForPdf(
        timeText: String
    ): String {
        val time = parseTime(timeText)
            ?: return timeText

        return time.format(
            DateTimeFormatter.ofPattern(
                "HH:mm",
                Locale.getDefault()
            )
        )
    }

    private fun parseDate(
        dateText: String
    ): LocalDate? {
        if (dateText.isBlank()) {
            return null
        }

        return try {
            LocalDate.parse(dateText)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun parseTime(
        timeText: String?
    ): LocalTime? {
        if (timeText.isNullOrBlank()) {
            return null
        }

        val acceptedFormats = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm")
        )

        acceptedFormats.forEach { formatter ->
            try {
                return LocalTime.parse(
                    timeText.trim(),
                    formatter
                )
            } catch (_: DateTimeParseException) {
// Try the next supported format.
            }
        }

        return null
    }

    private fun splitCommentIntoLines(
        comments: String,
        maximumCharactersPerLine: Int,
        maximumLines: Int
    ): List<String> {
        val cleanedComment = comments
            .replace("\n", " ")
            .trim()

        if (cleanedComment.isBlank()) {
            return emptyList()
        }

        val words = cleanedComment.split(
            Regex("""\s+""")
        )

        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (lines.size >= maximumLines) {
                break
            }

            val proposedLength =
                currentLine.length +
                        if (currentLine.isEmpty()) 0 else 1 +
                                word.length

            if (
                proposedLength <= maximumCharactersPerLine ||
                currentLine.isEmpty()
            ) {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }

                currentLine.append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }

        if (
            currentLine.isNotEmpty() &&
            lines.size < maximumLines
        ) {
            lines.add(currentLine.toString())
        }

        val usedWordCount = lines
            .joinToString(" ")
            .split(Regex("""\s+"""))
            .count { it.isNotBlank() }

        if (
            usedWordCount < words.size &&
            lines.isNotEmpty()
        ) {
            val finalIndex = lines.lastIndex
            val finalLine = lines[finalIndex]

            lines[finalIndex] = when {
                finalLine.length <= maximumCharactersPerLine - 3 ->
                    "$finalLine..."

                finalLine.length > 3 ->
                    finalLine
                        .take(maximumCharactersPerLine - 3)
                        .trimEnd() + "..."

                else ->
                    finalLine
            }
        }

        return lines
    }
}