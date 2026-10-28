package com.example.mission

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.EnumMap
import java.util.zip.CRC32

/**
 * QR Mission Engine & Generation Utility.
 *
 * Implements high-reliability, zero-trust, on-device QR code generation,
 * decoding, checksum calculation, and allowlist validation for Tahajjud waking missions.
 *
 * Security & Privacy Guarantees:
 * 1. Tokens are generated using [SecureRandom] with an on-device mathematical checksum.
 *    Format: TS-QR-<8_HEX_PAYLOAD><4_HEX_CHECKSUM> (e.g. TS-QR-9D4A71C8A3F2).
 * 2. Mathematical Integrity: A salted CRC32 checksum guarantees that arbitrary URLs,
 *    promotional barcodes, and corrupted codes are caught and rejected immediately on-device.
 * 3. Local Allowlist: Validation enforces that only codes explicitly registered
 *    in the device's local allowlist can dismiss alarms.
 * 4. Zero Network Transmission: All camera frames and QR payloads remain strictly in local RAM
 *    and are discarded after frame evaluation.
 * 5. Zero PII: Tokens contain no user identifiers, GPS coordinates, or sensitive secrets.
 */
object QrMissionEngine {

    const val TOKEN_PREFIX = "TS-QR-"
    private const val CHECKSUM_SALT = "TAHAJJUD_SQUAD_LOCAL_INTEGRITY_SALT_V1"
    private val TOKEN_REGEX = Regex("^TS-QR-([0-9A-F]{8})(-?)([0-9A-F]{4})$")
    private val secureRandom = SecureRandom()
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    /**
     * Calculates a 4-hex-digit checksum for an 8-hex-digit payload using CRC32 and app salt.
     */
    fun computeChecksum(payloadHex: String): String {
        val clean = payloadHex.uppercase().filter { it in "0123456789ABCDEF" }
        val crc = CRC32()
        crc.update("$clean:$CHECKSUM_SALT".toByteArray(Charsets.UTF_8))
        val value = (crc.value and 0xFFFFL).toInt()
        return String.format("%04X", value)
    }

    /**
     * Generates a new secure random mission token with an embedded on-device checksum.
     * Format: TS-QR-<8_HEX_PAYLOAD><4_HEX_CHECKSUM> (total 18 characters)
     * Example: TS-QR-9D4A71C8A3F2
     */
    fun generateSecureQrToken(): String {
        val bytes = ByteArray(4)
        secureRandom.nextBytes(bytes)
        val sb = StringBuilder()
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            sb.append(HEX_CHARS[v ushr 4])
            sb.append(HEX_CHARS[v and 0x0F])
        }
        val payload = sb.toString()
        val checksum = computeChecksum(payload)
        return "$TOKEN_PREFIX$payload$checksum"
    }

    /**
     * Validates whether a token string adheres to the secure Tahajjud Squad format.
     */
    fun isValidTokenFormat(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        return TOKEN_REGEX.matches(token.trim().uppercase())
    }

    /**
     * Verifies the mathematical on-device checksum of a mission token.
     * Returns true if the payload produces the expected checksum.
     */
    fun verifyTokenChecksum(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        val match = TOKEN_REGEX.matchEntire(token.trim().uppercase()) ?: return false
        val payload = match.groupValues[1]
        val providedChecksum = match.groupValues[3]
        val expectedChecksum = computeChecksum(payload)
        return expectedChecksum.equals(providedChecksum, ignoreCase = true)
    }

    private const val HASH_SALT = "TAHAJJUD_MISSION_HASH_SALT_2026"

    /**
     * Computes a cryptographically secure SHA-256 hash of a mission token with local salt.
     * Prevents storing or checking raw strings, protecting against memory dumping and logging leaks.
     */
    fun hashMissionToken(token: String): String {
        val clean = token.trim().uppercase()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest("$clean:$HASH_SALT".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Securely verifies a scanned token against a stored mission hash using constant-time comparison.
     */
    fun verifyScannedTokenHash(scannedToken: String?, storedMissionHash: String?): Boolean {
        if (scannedToken.isNullOrBlank() || storedMissionHash.isNullOrBlank()) return false
        val computedHash = hashMissionToken(scannedToken)
        return java.security.MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            storedMissionHash.trim().lowercase().toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Renders a QR code bitmap for printing or displaying on screen.
     * Uses Level H error correction (allows up to 30% damage/distortion recovery)
     * and a configurable quiet zone margin.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE,
        margin: Int = 2,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, margin)
                put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
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
     * Generates a high-contrast black & white QR code optimized for physical home printing.
     * Margin of 4 modules ensures high decodability even when taped onto a prayer mat or wall.
     */
    fun generateHighContrastPrintableQr(token: String, sizePx: Int = 800): Bitmap? {
        return generateQrBitmap(
            content = token,
            width = sizePx,
            height = sizePx,
            darkColor = Color.BLACK,
            lightColor = Color.WHITE,
            margin = 4,
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
    }

    /**
     * Generates a branded Islamic Emerald Green QR code for polished in-app visual display.
     */
    fun generateBrandedQrBitmap(token: String, sizePx: Int = 512): Bitmap? {
        return generateQrBitmap(
            content = token,
            width = sizePx,
            height = sizePx,
            darkColor = 0xFF123C2D.toInt(), // Emerald Green #123C2D
            lightColor = 0xFFFFFDF5.toInt(), // Warm White #FFFDF5
            margin = 2,
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
    }

    /**
     * Decodes a QR code string from an Android Bitmap locally in memory using the ZXing library.
     * Uses HybridBinarizer and TRY_HARDER for robust detection under varying dawn light conditions.
     */
    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                put(DecodeHintType.TRY_HARDER, true)
                put(DecodeHintType.CHARACTER_SET, "UTF-8")
                put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            }

            val result = QRCodeReader().decode(binaryBitmap, hints)
            result.text?.trim()
        } catch (_: Exception) {
            null
        }
    }

    sealed class QrValidationResult {
        data class ValidMatch(val token: String, val checksumVerified: Boolean = true) : QrValidationResult()
        open class InvalidCode(open val messageBn: String, open val messageEn: String) : QrValidationResult()
        data class ChecksumFailure(override val messageBn: String, override val messageEn: String) : InvalidCode(messageBn, messageEn)
        data class AllowlistRejected(override val messageBn: String, override val messageEn: String) : InvalidCode(messageBn, messageEn)
        data class InvalidFormat(override val messageBn: String, override val messageEn: String) : InvalidCode(messageBn, messageEn)
        object NotConfigured : QrValidationResult()
    }

    /**
     * Validates a scanned QR code against on-device checksum and the local allowlist.
     *
     * Verification Pipeline:
     * 1. Null/Blank check.
     * 2. Format validation (Token structure TS-QR-...).
     * 3. Cryptographic/Mathematical Checksum verification.
     * 4. Local Allowlist membership verification.
     */
    fun validateScannedCode(
        scannedCode: String?,
        registeredToken: String? = null,
        localAllowlist: Set<String> = emptySet(),
        requireChecksum: Boolean = true
    ): QrValidationResult {
        val effectiveAllowlist = buildSet {
            if (!registeredToken.isNullOrBlank()) add(registeredToken.trim().uppercase())
            addAll(localAllowlist.map { it.trim().uppercase() })
        }

        if (effectiveAllowlist.isEmpty()) {
            return QrValidationResult.NotConfigured
        }

        if (scannedCode.isNullOrBlank()) {
            return QrValidationResult.InvalidFormat(
                messageBn = "কোনো কিউআর কোড শনাক্ত করা যায়নি। অনুগ্রহ করে ক্যামেরার সামনে কোডটি সঠিকভাবে ধরুন।",
                messageEn = "No QR code detected. Please align your registered prayer mat code clearly in view."
            )
        }

        val cleanScanned = scannedCode.trim().uppercase()

        if (!isValidTokenFormat(cleanScanned)) {
            return QrValidationResult.InvalidFormat(
                messageBn = "অবৈধ ফরম্যাট! এটি তাহাজ্জুদ স্কোয়াডের কিউআর মিশন কোড নয়।",
                messageEn = "Invalid format! This is not a recognized Tahajjud Squad mission QR code."
            )
        }

        val isChecksumValid = verifyTokenChecksum(cleanScanned)
        if (requireChecksum && !isChecksumValid) {
            // If checksum fails, check if the token is explicitly in the user's local allowlist (legacy compatibility)
            if (!effectiveAllowlist.contains(cleanScanned)) {
                return QrValidationResult.ChecksumFailure(
                    messageBn = "চেকসাম ত্রুটি! কিউআর কোডটি বিকৃত বা অননুমোদিত।",
                    messageEn = "Checksum verification failed! The code is corrupted or counterfeit."
                )
            }
        }

        val isAllowed = effectiveAllowlist.contains(cleanScanned)
        return if (isAllowed) {
            QrValidationResult.ValidMatch(cleanScanned, checksumVerified = isChecksumValid)
        } else {
            QrValidationResult.AllowlistRejected(
                messageBn = "অননুমোদিত কিউআর কোড! এই কোডটি আপনার ডিভাইসের নিবন্ধিত এলাউলিস্টে নেই। জায়নামাজের সঠিক কোডটি স্ক্যান করুন।",
                messageEn = "Unauthorized QR Code! This code does not match your device's registered mission allowlist."
            )
        }
    }

    /**
     * Decodes a bitmap and validates the extracted code directly against the local allowlist.
     */
    fun decodeAndValidateBitmap(
        bitmap: Bitmap,
        registeredToken: String? = null,
        localAllowlist: Set<String> = emptySet(),
        requireChecksum: Boolean = true
    ): QrValidationResult {
        val decoded = decodeQrBitmap(bitmap)
        return validateScannedCode(decoded, registeredToken, localAllowlist, requireChecksum)
    }

    /**
     * Saves the printable QR bitmap to the sandboxed cache directory and returns a secure FileProvider Uri.
     */
    fun exportQrBitmapToCache(context: Context, bitmap: Bitmap, token: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "qr_codes")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val file = File(cacheDir, "tahajjud_qr_${token.replace('-', '_')}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates an Android Share Sheet Intent allowing the user to print, export to PDF,
     * or send the QR image to their computer/printer.
     */
    fun createShareQrIntent(context: Context, imageUri: Uri, token: String, isBengali: Boolean): Intent {
        val subject = if (isBengali) "তাহাজ্জুদ স্কোয়াড কিউআর কোড ($token)" else "Tahajjud Squad QR Code ($token)"
        val text = if (isBengali) {
            "তাহাজ্জুদ অ্যালার্মের কিউআর মিশন কোড (চেকসাম সুরক্ষিত)। এটি প্রিন্ট করে জায়নামাজে বা পড়ার টেবিলে রাখুন।\nটোকেন: $token"
        } else {
            "Tahajjud Squad Wake-Up Mission QR Code (Checksum Verified). Print and place this at your prayer spot.\nToken: $token"
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
