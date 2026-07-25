package nz.co.bealetimesheet.ui.signature

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object SignatureRepository {

    private const val SIGNATURE_FILE_NAME = "employee_signature.png"

    fun saveSignature(
        context: Context,
        bitmap: Bitmap
    ): Boolean {
        return try {
            val signatureFile = getSignatureFile(context)

            FileOutputStream(signatureFile).use { outputStream ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    outputStream
                )
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    fun loadSignature(
        context: Context
    ): Bitmap? {
        val signatureFile = getSignatureFile(context)

        if (!signatureFile.exists()) {
            return null
        }

        return BitmapFactory.decodeFile(
            signatureFile.absolutePath
        )
    }

    fun hasSignature(
        context: Context
    ): Boolean {
        return getSignatureFile(context).exists()
    }

    fun deleteSignature(
        context: Context
    ): Boolean {
        val signatureFile = getSignatureFile(context)

        return if (signatureFile.exists()) {
            signatureFile.delete()
        } else {
            true
        }
    }

    private fun getSignatureFile(
        context: Context
    ): File {
        return File(
            context.filesDir,
            SIGNATURE_FILE_NAME
        )
    }
}