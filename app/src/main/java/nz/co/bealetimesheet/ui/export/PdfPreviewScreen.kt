package nz.co.bealetimesheet.ui.export

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PdfPreviewScreen(
    pdfFile: File,
    recipientEmail: String,
    onRecipientEmailChange: (String) -> Unit,
    onSignAndSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val previewBitmap = remember(
        pdfFile.absolutePath,
        pdfFile.lastModified()
    ) {
        renderFirstPdfPage(pdfFile)
    }

    DisposableEffect(previewBitmap) {
        onDispose {
            if (!previewBitmap.isRecycled) {
                previewBitmap.recycle()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Timesheet Preview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Completed timesheet preview",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }

        OutlinedTextField(
            value = recipientEmail,
            onValueChange = onRecipientEmailChange,
            label = { Text("Recipient email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSignAndSubmit,
            enabled = recipientEmail.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign and Submit")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

private fun renderFirstPdfPage(pdfFile: File): Bitmap {
    val descriptor = ParcelFileDescriptor.open(
        pdfFile,
        ParcelFileDescriptor.MODE_READ_ONLY
    )

    PdfRenderer(descriptor).use { renderer ->
        require(renderer.pageCount > 0) {
            "The timesheet preview has no pages."
        }

        renderer.openPage(0).use { page ->
            val scale = 1.5f
            val bitmap = Bitmap.createBitmap(
                (page.width * scale).toInt(),
                (page.height * scale).toInt(),
                Bitmap.Config.ARGB_8888
            )

            bitmap.eraseColor(android.graphics.Color.WHITE)

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )

            return bitmap
        }
    }
}
