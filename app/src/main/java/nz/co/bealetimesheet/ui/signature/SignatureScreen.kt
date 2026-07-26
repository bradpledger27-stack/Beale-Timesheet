package nz.co.bealetimesheet.ui.signature

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun SignatureScreen(
    onSave: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val strokes = remember {
        mutableStateListOf<List<Offset>>()
    }

    var signaturePadSize = remember {
        IntSize.Zero
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Employee Signature",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Sign inside the box using your finger."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
                .onSizeChanged { size ->
                    signaturePadSize = size
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { startPosition ->
                            strokes.add(
                                listOf(startPosition)
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()

                            if (strokes.isNotEmpty()) {
                                val lastStrokeIndex =
                                    strokes.lastIndex

                                strokes[lastStrokeIndex] =
                                    strokes[lastStrokeIndex] +
                                            change.position
                            }
                        }
                    )
                }
        ) {
            strokes.forEach { strokePoints ->
                if (strokePoints.size == 1) {
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.Blue,
                        radius = 3f,
                        center = strokePoints.first()
                    )
                } else {
                    for (pointIndex in 0 until strokePoints.lastIndex) {
                        drawLine(
                            color = androidx.compose.ui.graphics.Color.Blue,
                            start = strokePoints[pointIndex],
                            end = strokePoints[pointIndex + 1],
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    strokes.clear()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (
                        strokes.isNotEmpty() &&
                        signaturePadSize.width > 0 &&
                        signaturePadSize.height > 0
                    ) {
                        val bitmap = createSignatureBitmap(
                            width = signaturePadSize.width,
                            height = signaturePadSize.height,
                            strokes = strokes
                        )

                        onSave(bitmap)
                    }
                },
                enabled = strokes.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Submit Timesheet")
            }
        }
    }
}

private fun createSignatureBitmap(
    width: Int,
    height: Int,
    strokes: List<List<Offset>>
): Bitmap {
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)

    canvas.drawColor(
        android.graphics.Color.TRANSPARENT
    )

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(25, 55, 180)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    strokes.forEach { strokePoints ->
        if (strokePoints.size == 1) {
            canvas.drawCircle(
                strokePoints.first().x,
                strokePoints.first().y,
                3f,
                paint
            )
        } else {
            for (pointIndex in 0 until strokePoints.lastIndex) {
                val startPoint = strokePoints[pointIndex]
                val endPoint = strokePoints[pointIndex + 1]

                canvas.drawLine(
                    startPoint.x,
                    startPoint.y,
                    endPoint.x,
                    endPoint.y,
                    paint
                )
            }
        }
    }

    return bitmap
}