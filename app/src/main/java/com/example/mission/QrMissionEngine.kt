package com.example.mission

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.security.SecureRandom
import java.util.EnumMap

object QrMissionEngine {

    private const val TOKEN_PREFIX = "TS-QR-"
    private val secureRandom = SecureRandom()
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    /**
     * Generates a new secure random mission token.
     * Example: TS-QR-9D4A71C8E3F2
     * Does not contain sensitive user data, passwords, or PII.
     */
    fun generateSecureQrToken(): String {
        val bytes = ByteArray(6)
        secureRandom.nextBytes(bytes)
        val sb = StringBuilder(TOKEN_PREFIX)
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            sb.append(HEX_CHARS[v ushr 4])
            sb.append(HEX_CHARS[v and 0x0F])
        }
        return sb.toString()
    }

    /**
     * Renders a QR code bitmap for printing or displaying on screen.
     * Styled in crisp black/white or Islamic Emerald Green and White.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            }

            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) darkColor else lightColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a QR code string from an Android Bitmap.
     */
    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = QRCodeReader().decode(binaryBitmap)
            result.text?.trim()
        } catch (e: Exception) {
            null
        }
    }

    sealed class QrValidationResult {
        object ValidMatch : QrValidationResult()
        data class InvalidCode(val messageBn: String, val messageEn: String) : QrValidationResult()
        object NotConfigured : QrValidationResult()
    }

    /**
     * Securely validates a scanned QR code against the user's registered token.
     * Prevents arbitrary QR codes, promotional URLs, or other objects from dismissing the alarm.
     */
    fun validateScannedCode(scannedCode: String?, registeredToken: String?): QrValidationResult {
        if (registeredToken.isNullOrBlank()) {
            return QrValidationResult.NotConfigured
        }
        if (scannedCode.isNullOrBlank()) {
            return QrValidationResult.InvalidCode(
                messageBn = "কোনো কিউআর কোড শনাক্ত করা যায়নি। অনুগ্রহ করে ক্যামেরার সামনে কোডটি সঠিকভাবে ধরুন।",
                messageEn = "No QR code detected. Please hold the registered code clearly in view."
            )
        }

        val cleanScanned = scannedCode.trim()
        val cleanRegistered = registeredToken.trim()

        return if (cleanScanned == cleanRegistered) {
            QrValidationResult.ValidMatch
        } else {
            QrValidationResult.InvalidCode(
                messageBn = "ভুল কিউআর কোড! এই কোডটি আপনার তাহাজ্জুদ স্কোয়াডের নিবন্ধিত কোড নয়। জায়নামাজে রাখা সঠিক কোডটি স্ক্যান করুন।",
                messageEn = "Invalid QR Code! This code does not match your registered Tahajjud mission code."
            )
        }
    }
}
