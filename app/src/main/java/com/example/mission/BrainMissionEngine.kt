package com.example.mission

import com.example.model.MathProblem
import com.example.model.MissionDifficulty
import com.example.model.TypingProblem
import java.util.UUID
import kotlin.random.Random

object BrainMissionEngine {

    private val typingPhrasesBn = listOf(
        "আস-সালাতু খাইরুম মিনান নাওম",
        "ঘুমের চেয়ে তাহাজ্জুদ ও সালাত উত্তম",
        "হে আল্লাহ! আমাকে শেষ রাতের ইবাদতে কবুল করুন",
        "রাতের নীরবতায় মহান আল্লাহর নৈকট্য লাভ",
        "তাহাজ্জুদের দু'আ কখনো বিফল হয় না",
        "হে রব! আমাকে ও আমার পরিবারকে ক্ষমা করুন",
        "ইস্তিগফার ও বিনম্র সালাতে অন্তর পরিশুদ্ধ হয়"
    )

    private val typingPhrasesEn = listOf(
        "As-Salatu Khairum Minan Naum",
        "Prayer is better than sleep",
        "O Allah, accept my Tahajjud and night prayers",
        "Rise in the night to seek Allah's mercy and forgiveness",
        "Consistency in night prayer brings peace to the heart",
        "Supplication in Tahajjud is like an arrow that hits the mark",
        "Wake up early and prepare for Fajr with devotion"
    )

    /**
     * Generates a randomized math problem based on the configured difficulty.
     * Answers are computed dynamically and never hardcoded.
     */
    fun generateMathProblem(difficulty: MissionDifficulty): MathProblem {
        val random = Random.Default
        val id = UUID.randomUUID().toString().take(8)

        return when (difficulty) {
            MissionDifficulty.EASY -> {
                val isAddition = random.nextBoolean()
                if (isAddition) {
                    val a = random.nextInt(12, 38)
                    val b = random.nextInt(8, 29)
                    MathProblem(id, "$a + $b = ?", a + b)
                } else {
                    val a = random.nextInt(25, 59)
                    val b = random.nextInt(11, a - 5)
                    MathProblem(id, "$a - $b = ?", a - b)
                }
            }
            MissionDifficulty.MEDIUM -> {
                val operationType = random.nextInt(3)
                when (operationType) {
                    0 -> { // Multiplication + addition
                        val a = random.nextInt(4, 9)
                        val b = random.nextInt(4, 9)
                        val c = random.nextInt(10, 25)
                        MathProblem(id, "$a × $b + $c = ?", (a * b) + c)
                    }
                    1 -> { // 3-term addition/subtraction
                        val a = random.nextInt(20, 45)
                        val b = random.nextInt(15, 35)
                        val c = random.nextInt(10, 20)
                        MathProblem(id, "$a + $b - $c = ?", a + b - c)
                    }
                    else -> { // Multiplication - subtraction
                        val a = random.nextInt(6, 12)
                        val b = random.nextInt(5, 10)
                        val c = random.nextInt(5, 18)
                        MathProblem(id, "$a × $b - $c = ?", (a * b) - c)
                    }
                }
            }
            MissionDifficulty.HARD -> {
                val operationType = random.nextInt(2)
                if (operationType == 0) {
                    val a = random.nextInt(11, 16)
                    val b = random.nextInt(6, 12)
                    val c = random.nextInt(20, 45)
                    MathProblem(id, "$a × $b - $c = ?", (a * b) - c)
                } else {
                    val a = random.nextInt(7, 12)
                    val b = random.nextInt(8, 14)
                    val c = random.nextInt(12, 28)
                    MathProblem(id, "($a × $b) + $c = ?", (a * b) + c)
                }
            }
        }
    }

    /**
     * Generates a randomized list of math problems according to task count.
     */
    fun generateMathProblems(difficulty: MissionDifficulty, count: Int): List<MathProblem> {
        val clampedCount = count.coerceIn(1, 5)
        return (1..clampedCount).map { generateMathProblem(difficulty) }
    }

    /**
     * Generates a typing challenge with an encouraging wake-up phrase.
     */
    fun generateTypingProblem(isBengali: Boolean): TypingProblem {
        val pool = if (isBengali) typingPhrasesBn else typingPhrasesEn
        val phrase = pool.random()
        val prompt = if (isBengali) {
            "নিচের বাক্যটি হুবহু নিচে টাইপ করে সাবমিট করুন:"
        } else {
            "Type the exact sentence below to confirm you are awake:"
        }
        return TypingProblem(
            id = UUID.randomUUID().toString().take(8),
            promptText = prompt,
            targetPhrase = phrase
        )
    }

    /**
     * Validates whether the user's typed input matches the target phrase.
     * Normalized for leading/trailing whitespaces and case-insensitivity.
     */
    fun validateTypingInput(input: String, target: String): Boolean {
        val cleanInput = input.trim().replace(Regex("\\s+"), " ")
        val cleanTarget = target.trim().replace(Regex("\\s+"), " ")
        return cleanInput.equals(cleanTarget, ignoreCase = true)
    }

    /**
     * Validates a math answer.
     */
    fun validateMathAnswer(userAnswerText: String, problem: MathProblem): Boolean {
        val parsed = userAnswerText.trim().toIntOrNull() ?: return false
        return parsed == problem.correctAnswer
    }
}
