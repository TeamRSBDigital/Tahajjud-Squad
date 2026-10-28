package com.example.mission

import android.graphics.Bitmap
import android.graphics.Color
import com.example.model.PhotoMatchResult
import kotlin.math.abs

/**
 * On-Device Lightweight Image Signature and Similarity Engine.
 *
 * Designed specifically for non-facial, on-device object/environment verification
 * (e.g., prayer mat, Quran stand, study desk) without cloud transmission or privacy invasion.
 *
 * Uses an 8x8 spatial grid with normalized RGB cell color vectors.
 * Computes mean spatial-color difference on-device with zero server dependencies.
 */
object ImageSignatureEngine {

    const val MATCH_THRESHOLD_PERCENT = 65
    private const val GRID_SIZE = 8 // 8x8 = 64 spatial cells

    /**
     * Extracts a compact, privacy-safe signature string from an on-device Bitmap.
     * The signature represents normalized spatial color distribution in an 8x8 grid.
     */
    fun computeSignature(bitmap: Bitmap): String {
        val scaled = Bitmap.createScaledBitmap(bitmap, GRID_SIZE, GRID_SIZE, true)
        val sb = StringBuilder()

        for (y in 0 until GRID_SIZE) {
            for (x in 0 until GRID_SIZE) {
                val pixel = scaled.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                sb.append(r.toString(16).padStart(2, '0'))
                sb.append(g.toString(16).padStart(2, '0'))
                sb.append(b.toString(16).padStart(2, '0'))
            }
        }

        return sb.toString()
    }

    /**
     * Compares a newly captured bitmap against the registered signature.
     * Evaluates similarity on a 0-100% scale.
     */
    fun compareImageToSignature(
        capturedBitmap: Bitmap,
        registeredSignature: String,
        isBengali: Boolean = true
    ): PhotoMatchResult {
        if (registeredSignature.isBlank()) {
            return PhotoMatchResult(
                isMatch = false,
                similarityPercent = 0,
                feedbackMessage = if (isBengali) {
                    "কোনো টার্গেট ছবি নিবন্ধিত নেই। অনুগ্রহ করে প্রথমে একটি বস্তু (যেমন জায়নামাজ বা পড়ার টেবিল) নিবন্ধিত করুন।"
                } else {
                    "No target photo registered. Please register a target object (e.g., prayer mat or desk) first."
                }
            )
        }

        val capturedSignature = computeSignature(capturedBitmap)
        val similarity = calculateSimilarity(capturedSignature, registeredSignature)

        val isMatch = similarity >= MATCH_THRESHOLD_PERCENT

        val feedback = if (isMatch) {
            if (isBengali) {
                "সফলভাবে মিলেছে! সাদৃশ্য: $similarity% (নূন্যতম $MATCH_THRESHOLD_PERCENT% প্রয়োজন ছিল)। অ্যালার্ম বন্ধ হচ্ছে।"
            } else {
                "Match verified! Similarity: $similarity% (threshold: $MATCH_THRESHOLD_PERCENT%). Dismissing alarm."
            }
        } else {
            if (isBengali) {
                "ছবি মেলেনি (সাদৃশ্য: $similarity%)। অনুগ্রহ করে পর্যাপ্ত আলোতে আপনার নিবন্ধিত বস্তুটির (জায়নামাজ/টেবিল) ছবি আবার তুলুন।"
            } else {
                "Photo did not match (similarity: $similarity%). Please capture the registered object in clear lighting and retry."
            }
        }

        return PhotoMatchResult(
            isMatch = isMatch,
            similarityPercent = similarity,
            feedbackMessage = feedback
        )
    }

    /**
     * Calculates spatial color similarity between two serialized signatures (0% - 100%).
     */
    fun calculateSimilarity(sigA: String, sigB: String): Int {
        if (sigA.isBlank() || sigB.isBlank()) return 0
        if (sigA == sigB) return 100

        val totalCells = GRID_SIZE * GRID_SIZE
        val cellHexLength = 6 // 2 hex chars each for R, G, B
        val minLen = minOf(sigA.length, sigB.length) / cellHexLength
        if (minLen < 1) return 0

        var totalDifference = 0.0

        for (i in 0 until minLen) {
            val cellA = sigA.substring(i * cellHexLength, (i + 1) * cellHexLength)
            val cellB = sigB.substring(i * cellHexLength, (i + 1) * cellHexLength)

            val rA = cellA.substring(0, 2).toIntOrNull(16) ?: 0
            val gA = cellA.substring(2, 4).toIntOrNull(16) ?: 0
            val bA = cellA.substring(4, 6).toIntOrNull(16) ?: 0

            val rB = cellB.substring(0, 2).toIntOrNull(16) ?: 0
            val gB = cellB.substring(2, 4).toIntOrNull(16) ?: 0
            val bB = cellB.substring(4, 6).toIntOrNull(16) ?: 0

            val diff = (abs(rA - rB) + abs(gA - gB) + abs(bA - bB)).toDouble() / (3.0 * 255.0)
            totalDifference += diff
        }

        val avgDifference = totalDifference / minLen.toDouble()
        val similarityFraction = (1.0 - avgDifference).coerceIn(0.0, 1.0)
        return (similarityFraction * 100).toInt()
    }
}
