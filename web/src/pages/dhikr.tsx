import React, { useState, useEffect } from 'react';
import { DhikrPreset } from '../types';
import { 
  Fingerprint, 
  RotateCcw, 
  Volume2, 
  VolumeX, 
  BookOpen, 
  Sparkles
} from 'lucide-react';

const DHIKR_PRESETS: DhikrPreset[] = [
  {
    id: 'subhanallah',
    arabicText: 'سُبْحَانَ اللَّهِ',
    bengaliTransliteration: 'সুবহানাল্লাহ',
    bengaliMeaning: 'আল্লাহ তাআলা যাবতীয় ত্রুটি থেকে পরম পবিত্র।',
    englishMeaning: 'Glory be to Allah.',
    reference: 'সহীহ মুসলিম: ৫৯৭ (প্রত্যেক সালাতের পর ৩৩ বার)',
    defaultTarget: 33,
  },
  {
    id: 'alhamdulillah',
    arabicText: 'الْحَمْدُ لِلَّهِ',
    bengaliTransliteration: 'আলহামদুলিল্লাহ',
    bengaliMeaning: 'সকল প্রশংসা ও কৃতজ্ঞতা একমাত্র আল্লাহর প্রাপ্য।',
    englishMeaning: 'All praise is due to Allah.',
    reference: 'সহীহ মুসলিম: ৫৯৭ (মিযানের পাল্লা পূর্ণকারী)',
    defaultTarget: 33,
  },
  {
    id: 'allahuakbar',
    arabicText: 'اللَّهُ أَكْبَرُ',
    bengaliTransliteration: 'আল্লাহু আকবার',
    bengaliMeaning: 'আল্লাহ সবচেয়ে মহান ও শ্রেষ্ঠ।',
    englishMeaning: 'Allah is the Greatest.',
    reference: 'সহীহ বুখারী: ৮৪৩ (সালাত পরবর্তী অন্যতম শ্রেষ্ঠ যিকির)',
    defaultTarget: 33,
  },
  {
    id: 'astaghfirullah',
    arabicText: 'أَسْتَغْفِرُ اللَّهَ',
    bengaliTransliteration: 'আস্তাগফিরুল্লাহ',
    bengaliMeaning: 'আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি।',
    englishMeaning: 'I seek forgiveness from Allah.',
    reference: 'সহীহ বুখারী: ৬৩০৭ (রাসূলুল্লাহ সা. দৈনিক ৭০ বারের বেশি ইস্তিগফার করতেন)',
    defaultTarget: 100,
  },
  {
    id: 'lailahaillallah',
    arabicText: 'لَا إِلَهَ إِلَّا اللَّهُ',
    bengaliTransliteration: 'লা ইলাহা ইল্লাল্লাহ',
    bengaliMeaning: 'আল্লাহ ছাড়া সত্য কোনো উপাস্য নেই।',
    englishMeaning: 'There is no deity worthy of worship except Allah.',
    reference: 'সুনানে তিরমিযী: ৩৩৮৩ (সর্বোত্তম যিকির)',
    defaultTarget: 100,
  },
  {
    id: 'subhanallah_bihamdihi',
    arabicText: 'سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ',
    bengaliTransliteration: 'সুবহানাল্লাহি ওয়া বিহামদিহী, সুবহানাল্লাহিল আযীম',
    bengaliMeaning: 'আল্লাহর প্রশংসাসহ তাঁর পবিত্রতা ঘোষণা করছি, মহান আল্লাহ অতীব পবিত্র।',
    englishMeaning: 'Glory be to Allah and His is the praise, Glory be to Allah the Supreme.',
    reference: 'সহীহ বুখারী: ৬৪০৬ (জিহ্বায় উচ্চারণে হালকা, মিযানের পাল্লায় ভারী)',
    defaultTarget: 100,
  },
  {
    id: 'durood',
    arabicText: 'اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ',
    bengaliTransliteration: 'আল্লাহুম্মা সাল্লি আলা মুহাম্মাদিন ওয়া আলা আলি মুহাম্মাদ',
    bengaliMeaning: 'হে আল্লাহ! মুহাম্মাদ (সা.) এবং তাঁর পরিবারের প্রতি শান্তি ও রহমত বর্ষণ করুন।',
    englishMeaning: 'O Allah, send blessings upon Muhammad and upon the family of Muhammad.',
    reference: 'সহীহ মুসলিম: ৪০৮ (একবার দরূদে ১০টি রহমত বর্ষিত হয়)',
    defaultTarget: 100,
  },
];

export default function DhikrPage() {
  const [selectedPreset, setSelectedPreset] = useState<DhikrPreset>(DHIKR_PRESETS[0]);
  const [count, setCount] = useState<number>(0);
  const [target, setTarget] = useState<number>(33);
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [completionMessage, setCompletionMessage] = useState<string | null>(null);

  useEffect(() => {
    const saved = localStorage.getItem(`dhikr_count_${selectedPreset.id}`);
    if (saved) {
      setCount(parseInt(saved, 10) || 0);
    } else {
      setCount(0);
    }
    setTarget(selectedPreset.defaultTarget);
  }, [selectedPreset]);

  const playClickSound = () => {
    if (!soundEnabled) return;
    try {
      const audioCtx = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, audioCtx.currentTime); // D5 tone
      gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.08);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.08);
    } catch {
      // AudioContext fallback
    }
  };

  const handleIncrement = () => {
    const newCount = count + 1;
    setCount(newCount);
    localStorage.setItem(`dhikr_count_${selectedPreset.id}`, newCount.toString());
    playClickSound();

    if (newCount === target) {
      setCompletionMessage(`মাশাআল্লাহ! "${selectedPreset.bengaliTransliteration}" ${target} বার পূর্ণ হয়েছে।`);
      setTimeout(() => setCompletionMessage(null), 4000);
    }
  };

  const handleReset = () => {
    setCount(0);
    localStorage.setItem(`dhikr_count_${selectedPreset.id}`, '0');
    setCompletionMessage(null);
  };

  const progressPercent = Math.min(100, Math.round((count / target) * 100));

  return (
    <div className="space-y-6">
      {/* Header in Emerald Green #123C2D with Royal Gold #D4AF57 */}
      <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] rounded-3xl p-6 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-[#D4AF57]">
            <Fingerprint className="w-5 h-5 text-[#D4AF57]" />
            <span className="text-xs font-semibold uppercase tracking-wider">মাসনূন যিকির ও তাসবীহ</span>
          </div>
          <h1 className="text-2xl font-black text-[#FFFDF5] mt-1">ডিজিটাল তাসবীহ ও আযকার</h1>
          <p className="text-sm text-[#D8E2DC]">হৃদয়ের প্রশান্তি ও আত্মশুদ্ধির সর্বোত্তম মাধ্যম</p>
        </div>

        {/* Audio / Reset Controls */}
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setSoundEnabled(!soundEnabled)}
            className="p-2.5 rounded-xl bg-[#0C291E]/80 border border-[#D4AF57]/30 text-[#FFFDF5] hover:text-[#D4AF57] transition"
            title={soundEnabled ? 'সাউন্ড বন্ধ করুন' : 'সাউন্ড চালু করুন'}
          >
            {soundEnabled ? <Volume2 className="w-4 h-4 text-[#D4AF57]" /> : <VolumeX className="w-4 h-4 text-[#D8E2DC]" />}
          </button>
          <button
            onClick={handleReset}
            className="px-3.5 py-2 rounded-xl bg-[#0C291E]/80 border border-[#D4AF57]/30 text-[#FFFDF5] hover:text-[#D4AF57] text-xs font-semibold flex items-center space-x-1.5 transition"
          >
            <RotateCcw className="w-3.5 h-3.5 text-[#D4AF57]" />
            <span>রিসেট</span>
          </button>
        </div>
      </div>

      {/* Completion Message */}
      {completionMessage && (
        <div className="p-4 bg-[#EAF5EE] border border-[#1E7E52]/30 text-[#123C2D] rounded-2xl text-sm flex items-center space-x-2">
          <Sparkles className="w-5 h-5 text-[#D4AF57] flex-shrink-0" />
          <span className="font-bold">{completionMessage}</span>
        </div>
      )}

      {/* Main Tasbih Card in Emerald Green #123C2D & Royal Gold #D4AF57 */}
      <div className="bg-gradient-to-br from-[#0C291E] via-[#123C2D] to-[#1A5641] text-[#FFFDF5] rounded-3xl p-6 md:p-10 border border-[#D4AF57]/40 shadow-xl text-center relative overflow-hidden">
        <div className="relative z-10 space-y-6">
          {/* Arabic and Meaning */}
          <div className="space-y-2 max-w-xl mx-auto">
            <h2 className="text-3xl md:text-4xl font-serif text-[#D4AF57] tracking-wide py-2 select-none">
              {selectedPreset.arabicText}
            </h2>
            <div className="text-xl font-bold text-[#FFFDF5] tracking-wide">
              {selectedPreset.bengaliTransliteration}
            </div>
            <p className="text-xs md:text-sm text-[#D8E2DC] italic">
              "{selectedPreset.bengaliMeaning}"
            </p>
            <div className="inline-flex items-center space-x-1.5 text-[11px] text-[#E5C678] bg-white/10 px-3.5 py-1 rounded-full border border-white/15">
              <BookOpen className="w-3.5 h-3.5" />
              <span>{selectedPreset.reference}</span>
            </div>
          </div>

          {/* Giant Circular Touch Button in Emerald & Royal Gold */}
          <div className="py-4">
            <button
              onClick={handleIncrement}
              className="w-48 h-48 md:w-56 md:h-56 mx-auto rounded-full bg-gradient-to-b from-[#1A5641] to-[#0C291E] border-4 border-[#D4AF57] shadow-2xl flex flex-col items-center justify-center transform active:scale-95 hover:border-[#E5C678] transition duration-150 cursor-pointer select-none group relative"
              style={{
                boxShadow: '0 0 30px rgba(212, 175, 87, 0.25)',
              }}
            >
              {/* Circular Progress Ring */}
              <svg className="absolute inset-0 w-full h-full -rotate-90 pointer-events-none" viewBox="0 0 100 100">
                <circle
                  cx="50"
                  cy="50"
                  r="46"
                  className="stroke-white/10"
                  strokeWidth="3"
                  fill="transparent"
                />
                <circle
                  cx="50"
                  cy="50"
                  r="46"
                  className="stroke-[#D4AF57] transition-all duration-300"
                  strokeWidth="3.5"
                  strokeDasharray="289"
                  strokeDashoffset={289 - (289 * progressPercent) / 100}
                  strokeLinecap="round"
                  fill="transparent"
                />
              </svg>

              <span className="text-4xl md:text-5xl font-mono font-black text-[#D4AF57] tracking-tight group-hover:scale-105 transition">
                {count}
              </span>
              <span className="text-xs text-[#D8E2DC] mt-1 uppercase tracking-widest font-semibold">
                লক্ষ্য: {target}
              </span>
              <span className="text-[10px] text-[#E5C678] mt-1">স্পর্শ করে গণনা করুন</span>
            </button>
          </div>

          {/* Target Selectors */}
          <div className="flex items-center justify-center space-x-3 pt-2">
            <span className="text-xs text-[#D8E2DC]">টার্গেট:</span>
            {[33, 100, 1000].map((t) => (
              <button
                key={t}
                onClick={() => setTarget(t)}
                className={`px-3.5 py-1 rounded-full text-xs font-bold transition ${
                  target === t
                    ? 'bg-[#D4AF57] text-[#123C2D] shadow'
                    : 'bg-white/10 text-[#FFFDF5] hover:bg-white/20 border border-white/15'
                }`}
              >
                {t} বার
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Preset Selector Grid in Warm White #FFFDF5 */}
      <div className="space-y-3">
        <h3 className="text-sm font-bold text-[#123C2D] uppercase tracking-wider">
          মাসনূন যিকিরসমূহ বেছে নিন
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
          {DHIKR_PRESETS.map((preset) => {
            const isCurrent = preset.id === selectedPreset.id;
            return (
              <div
                key={preset.id}
                onClick={() => setSelectedPreset(preset)}
                className={`p-4 rounded-2xl border cursor-pointer transition ${
                  isCurrent
                    ? 'bg-[#123C2D] text-[#FFFDF5] border-[#D4AF57] shadow-md'
                    : 'bg-[#FFFDF5] text-[#252B27] border-[#E5DFD1] hover:border-[#D4AF57]/60 hover:bg-[#FBF6E9]'
                }`}
              >
                <div className="flex items-start justify-between">
                  <span className={`text-base font-serif font-bold ${isCurrent ? 'text-[#D4AF57]' : 'text-[#123C2D]'}`}>
                    {preset.arabicText}
                  </span>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                    isCurrent ? 'bg-white/20 text-[#D4AF57]' : 'bg-[#F2EFE4] text-[#434E47]'
                  }`}>
                    {preset.defaultTarget} বার
                  </span>
                </div>
                <div className={`text-xs font-bold mt-1.5 ${isCurrent ? 'text-[#FFFDF5]' : 'text-[#252B27]'}`}>
                  {preset.bengaliTransliteration}
                </div>
                <p className={`text-[11px] mt-1 line-clamp-2 ${isCurrent ? 'text-[#D8E2DC]' : 'text-[#6B7870]'}`}>
                  {preset.bengaliMeaning}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
