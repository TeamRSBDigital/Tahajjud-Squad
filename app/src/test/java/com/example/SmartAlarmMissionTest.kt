package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AmalRepository
import com.example.mission.BrainMissionEngine
import com.example.mission.ImageSignatureEngine
import com.example.mission.QrMissionEngine
import com.example.model.AlarmMissionType
import com.example.model.MissionDifficulty
import com.example.model.SmartAlarmConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SmartAlarmMissionTest {

    @Test
    fun `brain mission math engine generates valid problems and validates answers correctly`() {
        // Easy difficulty
        val easyProblem = BrainMissionEngine.generateMathProblem(MissionDifficulty.EASY)
        assertNotNull(easyProblem.id)
        assertTrue(easyProblem.questionText.contains("?"))
        assertTrue("Easy answer must be validated", BrainMissionEngine.validateMathAnswer(easyProblem.correctAnswer.toString(), easyProblem))
        assertFalse("Wrong answer must fail", BrainMissionEngine.validateMathAnswer((easyProblem.correctAnswer + 5).toString(), easyProblem))
        assertFalse("Malformed text answer must fail", BrainMissionEngine.validateMathAnswer("abc", easyProblem))

        // Medium difficulty
        val mediumProblems = BrainMissionEngine.generateMathProblems(MissionDifficulty.MEDIUM, 3)
        assertEquals(3, mediumProblems.size)
        mediumProblems.forEach { problem ->
            assertTrue(BrainMissionEngine.validateMathAnswer(problem.correctAnswer.toString(), problem))
        }

        // Hard difficulty
        val hardProblem = BrainMissionEngine.generateMathProblem(MissionDifficulty.HARD)
        assertTrue(BrainMissionEngine.validateMathAnswer(hardProblem.correctAnswer.toString(), hardProblem))
    }

    @Test
    fun `brain mission typing engine generates phrases and validates case and whitespace normalization`() {
        val bnProblem = BrainMissionEngine.generateTypingProblem(isBengali = true)
        assertNotNull(bnProblem.targetPhrase)
        assertTrue(bnProblem.targetPhrase.isNotBlank())

        // Exact match
        assertTrue(BrainMissionEngine.validateTypingInput(bnProblem.targetPhrase, bnProblem.targetPhrase))
        // Match with extra whitespace
        assertTrue(BrainMissionEngine.validateTypingInput("  ${bnProblem.targetPhrase}  ", bnProblem.targetPhrase))

        val enProblem = BrainMissionEngine.generateTypingProblem(isBengali = false)
        // Case-insensitive match for English
        assertTrue(BrainMissionEngine.validateTypingInput(enProblem.targetPhrase.lowercase(), enProblem.targetPhrase))
        assertTrue(BrainMissionEngine.validateTypingInput(enProblem.targetPhrase.uppercase(), enProblem.targetPhrase))

        // Wrong input must fail
        assertFalse(BrainMissionEngine.validateTypingInput("Unrelated text", enProblem.targetPhrase))
    }

    @Test
    fun `qr mission generates secure token and strictly rejects arbitrary qr codes`() {
        val token = QrMissionEngine.generateSecureQrToken()
        assertTrue("Token must start with prefix TS-QR-", token.startsWith("TS-QR-"))
        assertEquals(18, token.length)

        // Valid match
        val validResult = QrMissionEngine.validateScannedCode(token, token)
        assertTrue("Matching code must succeed", validResult is QrMissionEngine.QrValidationResult.ValidMatch)

        // Matching with leading/trailing spaces
        val spacedResult = QrMissionEngine.validateScannedCode("  $token  ", token)
        assertTrue("Whitespace-padded code must succeed", spacedResult is QrMissionEngine.QrValidationResult.ValidMatch)

        // Arbitrary URL or unrelated QR code must be rejected
        val invalidUrl = "https://example.com/unrelated"
        val invalidResult = QrMissionEngine.validateScannedCode(invalidUrl, token)
        assertTrue("Arbitrary URL must be rejected", invalidResult is QrMissionEngine.QrValidationResult.InvalidCode)

        // Null/Empty scanned code
        val emptyResult = QrMissionEngine.validateScannedCode("", token)
        assertTrue("Empty scan must be rejected", emptyResult is QrMissionEngine.QrValidationResult.InvalidCode)

        // Unconfigured token
        val unconfiguredResult = QrMissionEngine.validateScannedCode(token, "")
        assertTrue("Unconfigured token must return NotConfigured", unconfiguredResult is QrMissionEngine.QrValidationResult.NotConfigured)
    }

    @Test
    fun `qr bitmap roundtrip encode and decode`() {
        val testToken = "TS-QR-ABC123456789"
        val bitmap = QrMissionEngine.generateQrBitmap(testToken, 256, 256)
        assertNotNull("QR Bitmap should be generated", bitmap)

        val decoded = QrMissionEngine.decodeQrBitmap(bitmap!!)
        assertEquals("Decoded token should match original", testToken, decoded)
    }

    @Test
    fun `image signature engine computes on-device signatures and calculates similarity`() {
        // Create test green bitmap (like an emerald prayer mat)
        val greenBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        greenBitmap.eraseColor(Color.rgb(20, 83, 45))

        val greenSignature = ImageSignatureEngine.computeSignature(greenBitmap)
        assertNotNull(greenSignature)
        assertEquals(384, greenSignature.length) // 64 cells * 6 hex characters

        // Comparing same image gives 100% match
        val matchSelf = ImageSignatureEngine.compareImageToSignature(greenBitmap, greenSignature, isBengali = true)
        assertTrue("Self match must succeed", matchSelf.isMatch)
        assertEquals(100, matchSelf.similarityPercent)

        // Create vastly different bright red bitmap
        val redBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        redBitmap.eraseColor(Color.RED)

        val redSignature = ImageSignatureEngine.computeSignature(redBitmap)
        val similarityBetweenGreenAndRed = ImageSignatureEngine.calculateSimilarity(greenSignature, redSignature)
        assertTrue("Vastly different colors must have lower similarity", similarityBetweenGreenAndRed < ImageSignatureEngine.MATCH_THRESHOLD_PERCENT)

        val matchFailure = ImageSignatureEngine.compareImageToSignature(redBitmap, greenSignature, isBengali = true)
        assertFalse("Different photo must not match", matchFailure.isMatch)
    }

    @Test
    fun `smart alarm repository persists preferences and updates configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = AmalRepository(context)

        val initialConfig = repo.smartAlarmConfig.value
        assertNotNull(initialConfig)
        assertNotNull(initialConfig.registeredQrToken)

        val updatedConfig = initialConfig.copy(
            enabled = true,
            missionType = AlarmMissionType.QR_CODE,
            difficulty = MissionDifficulty.HARD,
            taskCount = 3
        )
        repo.updateSmartAlarmConfig(updatedConfig)

        val fetchedConfig = repo.smartAlarmConfig.value
        assertEquals(AlarmMissionType.QR_CODE, fetchedConfig.missionType)
        assertEquals(MissionDifficulty.HARD, fetchedConfig.difficulty)
        assertEquals(3, fetchedConfig.taskCount)

        // Test QR token regeneration
        val oldToken = fetchedConfig.registeredQrToken
        val newToken = repo.regenerateQrToken()
        assertNotEquals(oldToken, newToken)
        assertEquals(newToken, repo.smartAlarmConfig.value.registeredQrToken)

        // Test photo target registration
        repo.registerPhotoTarget("SIG_12345:AABBCC", "আমার জায়নামাজ")
        assertEquals("SIG_12345:AABBCC", repo.smartAlarmConfig.value.registeredTargetSignature)
        assertEquals("আমার জায়নামাজ", repo.smartAlarmConfig.value.registeredTargetName)

        repo.clearPhotoTarget()
        assertEquals("", repo.smartAlarmConfig.value.registeredTargetSignature)
        assertEquals("", repo.smartAlarmConfig.value.registeredTargetName)
    }
}
