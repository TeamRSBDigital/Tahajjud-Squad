package com.example.model

object AuthenticDhikrCatalog {

    val presets: List<DhikrPreset> = listOf(
        DhikrPreset(
            id = "dhikr_subhanallah",
            arabicText = "سُبْحَانَ اللهِ",
            bengaliTransliteration = "সুবহানাল্লাহ",
            bengaliMeaning = "আল্লাহ মহাপবিত্র ও সমস্ত ত্রুটি থেকে মুক্ত।",
            englishMeaning = "Glory be to Allah.",
            reference = "সহীহ মুসলিম (হাদীস নং ২৭৯৮) — তাসবীহ ফাতেমী ও সালাত পরবর্তী সুন্নাত আমল।",
            defaultTarget = 33
        ),
        DhikrPreset(
            id = "dhikr_alhamdulillah",
            arabicText = "الْحَمْدُ لِلَّهِ",
            bengaliTransliteration = "আলহামদুলিল্লাহ",
            bengaliMeaning = "যাবতীয় প্রশংসা ও কৃতজ্ঞতা একমাত্র মহান আল্লাহর জন্য।",
            englishMeaning = "All praise is due to Allah.",
            reference = "সহীহ মুসলিম (হাদীস নং ২২৩) — 'আলহামদুলিল্লাহ' মীযানকে পূর্ণ করে দেয়।",
            defaultTarget = 33
        ),
        DhikrPreset(
            id = "dhikr_allahuakbar",
            arabicText = "اللهُ أَكْبَرُ",
            bengaliTransliteration = "আল্লাহু আকবার",
            bengaliMeaning = "আল্লাহ সর্বশ্রেষ্ঠ ও মহান।",
            englishMeaning = "Allah is the Greatest.",
            reference = "সহীহ বুখারী (হাদীস নং ৬৩১৮) — সালাত পরবর্তী ও দৈনন্দিন যিকির।",
            defaultTarget = 34
        ),
        DhikrPreset(
            id = "dhikr_astaghfirullah",
            arabicText = "أَسْتَغْفِرُ اللهَ",
            bengaliTransliteration = "আস্তাগফিরুল্লাহ",
            bengaliMeaning = "আমি মহান আল্লাহর নিকট ক্ষমা প্রার্থনা করছি।",
            englishMeaning = "I seek forgiveness from Allah.",
            reference = "সহীহ বুখারী (হাদীস নং ৬৩০৭) — রাসূলুল্লাহ ﷺ দিনে সত্তরের অধিকবার তাওবা ও ইস্তিগফার করতেন।",
            defaultTarget = 100
        ),
        DhikrPreset(
            id = "dhikr_tahlil",
            arabicText = "لَا إِلَٰهَ إِلَّا اللهُ",
            bengaliTransliteration = "লা ইলাহা ইল্লাল্লাহ",
            bengaliMeaning = "আল্লাহ ছাড়া সত্য কোনো উপাস্য নেই।",
            englishMeaning = "There is no true deity worthy of worship except Allah.",
            reference = "সুনান আত-তিরমিযী (হাদীস নং ৩৩৮৩, হাসান) — সর্বোত্তম যিকির হলো 'লা ইলাহা ইল্লাল্লাহ'।",
            defaultTarget = 100
        ),
        DhikrPreset(
            id = "dhikr_salawat",
            arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
            bengaliTransliteration = "আল্লাহুম্মা সাল্লি 'আলা মুহাম্মাদিন ওয়া 'আলা আলি মুহাম্মাদ",
            bengaliMeaning = "হে আল্লাহ! আপনি মুহাম্মাদ ﷺ ও তাঁর পরিবারের উপর শান্তি ও রহমত বর্ষণ করুন।",
            englishMeaning = "O Allah, send blessings upon Muhammad and upon the family of Muhammad.",
            reference = "সহীহ মুসলিম (হাদীস নং ৪০৮) — যে ব্যক্তি একবার দরূদ পড়ে আল্লাহ তার উপর দশটি রহমত নাযিল করেন।",
            defaultTarget = 100
        ),
        DhikrPreset(
            id = "dhikr_subhanallahi_wa_bihamdihi",
            arabicText = "سُبْحَانَ اللهِ وَبِحَمْدِهِ ، سُبْحَانَ اللهِ الْعَظِيمِ",
            bengaliTransliteration = "সুবহানাল্লাহি ওয়া বিহামদিহি, সুবহানাল্লাহিল 'আযীম",
            bengaliMeaning = "আল্লাহর পবিত্রতা ঘোষণা করছি তাঁর প্রশংসাসহ; পরম মহান আল্লাহ অতি পবিত্র।",
            englishMeaning = "Glory be to Allah and His is the praise; Glory be to Allah, the Supreme.",
            reference = "সহীহ বুখারী (হাদীস নং ৬৬৮২) — দুটি বাক্য মুখে উচ্চারণ সহজ, মীযানে ভারী ও দয়াময়ের নিকট অত্যন্ত প্রিয়।",
            defaultTarget = 100
        ),
        DhikrPreset(
            id = "dhikr_hawqalah",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ",
            bengaliTransliteration = "লা হাওলা ওয়া লা কুওয়াতা ইল্লা বিল্লাহ",
            bengaliMeaning = "কোনো শক্তি ও সামর্থ্য নেই একমাত্র আল্লাহর সাহায্য ছাড়া।",
            englishMeaning = "There is no power nor strength except with Allah.",
            reference = "সহীহ বুখারী (হাদীস নং ৪২০৫) — জান্নাতের রত্নভাণ্ডারের অন্যতম ধন।",
            defaultTarget = 33
        )
    )
}
