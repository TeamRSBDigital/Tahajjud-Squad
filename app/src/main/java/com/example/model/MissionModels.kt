package com.example.model

enum class AlarmMissionType(
    val code: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String
) {
    NONE(
        code = "NONE",
        titleBn = "সাধারণ ডিসমিস",
        titleEn = "Standard Dismiss",
        descriptionBn = "সাধারণ অ্যালার্মের মতো একটি ট্যাপেই বন্ধ হবে।",
        descriptionEn = "Conventional alarm dismissal with one tap."
    ),
    BRAIN_MATH(
        code = "BRAIN_MATH",
        titleBn = "গণিত সমাধান মিশন",
        titleEn = "Brain Math Mission",
        descriptionBn = "ঘুম ভাঙাতে সহজ ও চিত্তাকর্ষক যোগ-বিয়োগ-গুণ সমাধান করুন।",
        descriptionEn = "Solve randomized mental arithmetic to activate your alertness."
    ),
    BRAIN_TYPING(
        code = "BRAIN_TYPING",
        titleBn = "বাক্য টাইপিং মিশন",
        titleEn = "Brain Typing Mission",
        descriptionBn = "প্রদর্শিত তাহাজ্জুদ ও সালাতের ফজিলতপূর্ণ বাক্যটি হুবহু টাইপ করুন।",
        descriptionEn = "Type an uplifting wake-up phrase to regain focus and wakefulness."
    ),
    QR_CODE(
        code = "QR_CODE",
        titleBn = "কিউআর কোড মিশন",
        titleEn = "QR Code Mission",
        descriptionBn = "জায়নামাজ বা পড়ার টেবিলে রাখা নির্দিষ্ট কিউআর কোড স্ক্যান করে অ্যালার্ম বন্ধ করুন।",
        descriptionEn = "Walk over and scan your registered QR code placed at your prayer area."
    ),
    PHOTO_TARGET(
        code = "PHOTO_TARGET",
        titleBn = "ক্যামেরা ছবি মিশন",
        titleEn = "Camera Photo Mission",
        descriptionBn = "পূর্বে নিবন্ধিত জায়নামাজ, কুরআন বা টেবিলের সঠিক ছবি তুলুন।",
        descriptionEn = "Capture an image matching your configured prayer mat or object."
    )
}

enum class MissionDifficulty(
    val code: String,
    val labelBn: String,
    val labelEn: String,
    val descriptionBn: String
) {
    EASY(
        code = "EASY",
        labelBn = "সহজ",
        labelEn = "Easy",
        descriptionBn = "সহজ ২ সংখ্যার যোগ বা বিয়োগ (১০-৫০ এর মধ্যে)"
    ),
    MEDIUM(
        code = "MEDIUM",
        labelBn = "মাঝারি",
        labelEn = "Medium",
        descriptionBn = "২-৩ ধাপের হিসাব এবং গুণসহ সমাধান"
    ),
    HARD(
        code = "HARD",
        labelBn = "কঠিন",
        labelEn = "Hard",
        descriptionBn = "বহুধাপের মানসিক চ্যালেঞ্জ ও নির্ভুল হিসাব"
    )
}

data class SmartAlarmConfig(
    val enabled: Boolean = true,
    val missionType: AlarmMissionType = AlarmMissionType.BRAIN_MATH,
    val difficulty: MissionDifficulty = MissionDifficulty.EASY,
    val taskCount: Int = 2,
    val registeredQrToken: String = "",
    val registeredTargetSignature: String = "",
    val registeredTargetName: String = "",
    val emergencyFallbackAllowed: Boolean = true,
    val emergencyPressCountRequired: Int = 8,
    val soundVolume: Float = 1.0f,
    val vibrate: Boolean = true
)

data class MathProblem(
    val id: String,
    val questionText: String,
    val correctAnswer: Int
)

data class TypingProblem(
    val id: String,
    val promptText: String,
    val targetPhrase: String
)

data class PhotoMatchResult(
    val isMatch: Boolean,
    val similarityPercent: Int,
    val feedbackMessage: String
)
