import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Moon, Clock, BookOpen, Users, Fingerprint, ChevronRight, Sparkles } from 'lucide-react';
import { getNextUpcomingTahajjud, formatTimeAmPm } from '@/lib/prayerCalculator';
import { PrayerStatus, PrayerType } from '@/types';
import { useAuth } from '@/context/AuthContext';

export default function HomeDashboard() {
  const { user, profile } = useAuth();
  const [prayers, setPrayers] = useState<Record<PrayerType, PrayerStatus>>({
    FAJR: 'PERFORMED',
    DHUHR: 'NOT_RECORDED',
    ASR: 'NOT_RECORDED',
    MAGHRIB: 'NOT_RECORDED',
    ISHA: 'NOT_RECORDED'
  });

  const [tahajjudInfo, setTahajjudInfo] = useState<{ fajr: string; tahajjud: string; remaining: string; hours: number; minutes: number }>({
    fajr: '04:45 AM',
    tahajjud: '04:15 AM',
    remaining: '০৩ ঘণ্টা ২৫ মিনিট বাকি',
    hours: 3,
    minutes: 25
  });

  const [dhikrCount, setDhikrCount] = useState(17);

  useEffect(() => {
    const next = getNextUpcomingTahajjud();
    const diff = next.tahajjud.getTime() - Date.now();
    let remainingStr = "সময় নিকটবর্তী";
    let h = 0;
    let m = 0;
    if (diff > 0) {
      h = Math.floor(diff / (1000 * 60 * 60));
      m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      remainingStr = `${h} ঘণ্টা ${m} মিনিট বাকি`;
    }
    setTahajjudInfo({
      fajr: formatTimeAmPm(next.fajr),
      tahajjud: formatTimeAmPm(next.tahajjud),
      remaining: remainingStr,
      hours: h,
      minutes: m
    });
  }, []);

  const togglePrayer = (p: PrayerType) => {
    const cycle: Record<PrayerStatus, PrayerStatus> = {
      NOT_RECORDED: 'PERFORMED',
      PERFORMED: 'MISSED',
      MISSED: 'QAZA',
      QAZA: 'NOT_RECORDED'
    };
    setPrayers(prev => ({ ...prev, [p]: cycle[prev[p]] }));
  };

  const performedCount = Object.values(prayers).filter(s => s === 'PERFORMED').length;
  const displayName = profile?.displayName || user?.displayName || 'মুহাম্মদ আব্দুল্লাহ';

  return (
    <div className="space-y-6">
      {/* Welcome Greeting Banner in Emerald Green #123C2D with Royal Gold #D4AF57 */}
      <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] rounded-3xl p-6 md:p-8 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-xl relative overflow-hidden">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 opacity-10 bg-radial from-white to-transparent pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-white/10 backdrop-blur-sm border border-[#D4AF57]/50 text-xs font-semibold text-[#E5C678]">
              <Sparkles className="w-3.5 h-3.5 text-[#D4AF57]" />
              <span>দৈনন্দিন আমল ও তাহাজ্জুদ ড্যাশবোর্ড</span>
            </div>
            <h1 className="text-2xl md:text-3xl font-extrabold mt-2 text-[#FFFDF5] tracking-tight">
              আসসালামু আলাইকুম, {displayName}
            </h1>
            <p className="text-sm text-[#D8E2DC] mt-1 max-w-xl">
              ফজর ও তাহাজ্জুদ রক্ষায় অবিচল থাকুন • {profile?.calculationCity || 'ঢাকা, বাংলাদেশ'}
            </p>
          </div>

          <div className="bg-[#0C291E]/80 backdrop-blur-md border border-[#D4AF57]/40 rounded-2xl px-5 py-3 text-center md:text-right">
            <span className="text-[11px] text-[#D8E2DC] uppercase tracking-wider block font-medium">আজকের সালাত সম্পন্ন</span>
            <span className="text-2xl font-black text-[#D4AF57]">{performedCount} / ৫ ওয়াক্ত</span>
          </div>
        </div>
      </div>

      {/* Main Grid: Tahajjud Countdown as Primary Focus alongside Prayer Checklist */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Tahajjud Countdown Card (Primary Visual Focus in Emerald Green & Royal Gold) */}
        <div className="bg-gradient-to-br from-[#0C291E] to-[#123C2D] rounded-3xl p-6 text-[#FFFDF5] border-2 border-[#D4AF57] shadow-xl relative flex flex-col justify-between overflow-hidden">
          <div className="absolute -right-8 -bottom-8 w-40 h-40 rounded-full bg-[#D4AF57]/10 pointer-events-none" />
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2.5">
                <div className="w-10 h-10 rounded-2xl bg-[#1A5641] border border-[#D4AF57] flex items-center justify-center text-[#D4AF57] shadow">
                  <Moon className="w-5 h-5 text-[#D4AF57]" />
                </div>
                <div>
                  <div className="flex items-center space-x-2">
                    <h2 className="text-lg font-bold text-[#E5C678]">তাহাজ্জুদ রিমাইন্ডার</h2>
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#D4AF57] text-[#123C2D]">
                      অগ্রাধিকার
                    </span>
                  </div>
                  <span className="text-xs text-[#D8E2DC]">রাতের শেষ তৃতীয়াংশ ও মুনাজাতের শ্রেষ্ঠ সময়</span>
                </div>
              </div>
              <span className="text-xs bg-[#1A5641] text-[#E5C678] px-3 py-1 rounded-full border border-[#D4AF57]/50 font-medium">
                ফজরের ৩০ মিনিট আগে
              </span>
            </div>

            {/* Countdown Big Display */}
            <div className="mt-5 bg-[#081C15]/70 rounded-2xl p-4 border border-[#D4AF57]/30 text-center">
              <span className="text-xs uppercase tracking-widest text-[#D8E2DC] font-semibold block">
                তাহাজ্জুদের ওয়াক্ত আরম্ভ হতে আর মাত্র
              </span>
              <div className="flex items-center justify-center space-x-3 mt-2">
                <div className="bg-[#123C2D] border border-[#D4AF57]/50 rounded-xl px-4 py-2 min-w-[70px]">
                  <span className="text-2xl sm:text-3xl font-black text-[#D4AF57]">
                    {String(tahajjudInfo.hours).padStart(2, '0')}
                  </span>
                  <span className="block text-[10px] text-[#D8E2DC] font-medium">ঘণ্টা</span>
                </div>
                <span className="text-2xl font-bold text-[#D4AF57]">:</span>
                <div className="bg-[#123C2D] border border-[#D4AF57]/50 rounded-xl px-4 py-2 min-w-[70px]">
                  <span className="text-2xl sm:text-3xl font-black text-[#D4AF57]">
                    {String(tahajjudInfo.minutes).padStart(2, '0')}
                  </span>
                  <span className="block text-[10px] text-[#D8E2DC] font-medium">মিনিট</span>
                </div>
              </div>
              <div className="mt-2.5 inline-flex items-center space-x-1.5 text-xs text-[#E5C678] font-medium">
                <Clock className="w-3.5 h-3.5 text-[#D4AF57]" />
                <span>{tahajjudInfo.remaining}</span>
              </div>
            </div>

            {/* Timings Row */}
            <div className="grid grid-cols-2 gap-3 mt-4">
              <div className="bg-[#0C291E]/90 border border-[#D4AF57]/20 rounded-xl p-3">
                <span className="text-[11px] text-[#D8E2DC] block">তাহাজ্জুদ অ্যালার্ম:</span>
                <p className="text-xl font-bold text-[#D4AF57] mt-0.5">{tahajjudInfo.tahajjud}</p>
              </div>
              <div className="bg-[#0C291E]/90 border border-[#D4AF57]/20 rounded-xl p-3 text-right">
                <span className="text-[11px] text-[#D8E2DC] block">ফজর শুরু:</span>
                <p className="text-xl font-bold text-[#FFFDF5] mt-0.5">{tahajjudInfo.fajr}</p>
              </div>
            </div>
          </div>

          <div className="mt-5 pt-4 border-t border-[#D4AF57]/30 flex items-center justify-between">
            <span className="text-xs text-[#D8E2DC]">হাদিস: শেষ রাতে আল্লাহ নিকটতম আকাশে নেমে আসেন</span>
            <Link href="/tahajjud" className="text-xs font-bold text-[#D4AF57] flex items-center hover:text-[#E5C678] transition">
              <span>তাহাজ্জুদ লগ করুন</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>
        </div>

        {/* 5 Daily Prayers Checklist (Warm White #FFFDF5 Surface with Deep Charcoal #252B27 text) */}
        <div className="bg-[#FFFDF5] rounded-3xl p-6 border border-[#E5DFD1] shadow-sm flex flex-col justify-between text-[#252B27]">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-base font-bold text-[#123C2D]">পাঁচ ওয়াক্ত ফরজ সালাত</h2>
                <p className="text-xs text-[#434E47]">ট্যাপ করে ওয়াক্তের স্ট্যাটাস পরিবর্তন করুন</p>
              </div>
              <div className="text-right">
                <span className="text-xl font-black text-[#123C2D]">{performedCount}/৫</span>
                <span className="block text-[10px] text-[#6B7870]">ওয়াক্ত আদায়কৃত</span>
              </div>
            </div>

            <div className="grid grid-cols-5 gap-2 mt-4">
              {(['FAJR', 'DHUHR', 'ASR', 'MAGHRIB', 'ISHA'] as PrayerType[]).map((p) => {
                const status = prayers[p];
                const labels: Record<PrayerType, string> = {
                  FAJR: 'ফজর',
                  DHUHR: 'যোহর',
                  ASR: 'আসর',
                  MAGHRIB: 'মাগরিব',
                  ISHA: 'ইশা'
                };
                const statusColors: Record<PrayerStatus, string> = {
                  PERFORMED: 'bg-[#123C2D] text-[#FFFDF5] border-[#123C2D] shadow-sm',
                  MISSED: 'bg-[#9E4040] text-[#FFFDF5] border-[#9E4040] shadow-sm',
                  QAZA: 'bg-[#D4AF57] text-[#123C2D] border-[#D4AF57] font-bold shadow-sm',
                  NOT_RECORDED: 'bg-[#F2EFE4] hover:bg-[#E5DFD1] text-[#252B27] border-[#E5DFD1]'
                };

                return (
                  <button
                    key={p}
                    onClick={() => togglePrayer(p)}
                    className={`py-3.5 px-1 rounded-2xl flex flex-col items-center justify-center transition border font-semibold ${statusColors[status]}`}
                  >
                    <span className="text-xs font-bold">{labels[p]}</span>
                    <span className="text-[10px] mt-1 opacity-90 truncate max-w-full font-medium">
                      {status === 'PERFORMED' ? 'আদায়' : status === 'MISSED' ? 'ছুটেছে' : status === 'QAZA' ? 'কাযা' : 'বাকি'}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-[#E5DFD1] flex items-center justify-between">
            <span className="text-xs text-[#6B7870]">সময়মতো সালাত আদায় মুমিনের শ্রেষ্ঠ আমল</span>
            <Link href="/prayer" className="text-xs font-bold text-[#123C2D] flex items-center hover:text-[#1A5641]">
              <span>বিস্তারিত সালাত লগ</span>
              <ChevronRight className="w-3.5 h-3.5 ml-1" />
            </Link>
          </div>
        </div>
      </div>

      {/* Squad, Amal & Dhikr Highlights in Warm White & Emerald */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Active Squad Card */}
        <Link href="/squads" className="bg-[#FFFDF5] hover:bg-[#FBF6E9] transition rounded-3xl p-5 border border-[#E5DFD1] shadow-sm flex flex-col justify-between group text-[#252B27]">
          <div>
            <div className="flex items-center space-x-2.5">
              <div className="w-10 h-10 rounded-2xl bg-[#EBF4F0] text-[#123C2D] flex items-center justify-center border border-[#123C2D]/20">
                <Users className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-[#123C2D] group-hover:text-[#1A5641] transition">তাহাজ্জুদ স্কোয়াড</h3>
                <span className="text-[10px] text-[#6B7870]">পারস্পরিক অনুপ্রেরণা</span>
              </div>
            </div>
            <p className="text-xs text-[#434E47] mt-3 leading-relaxed">
              ফজর ও তাহাজ্জুদ রক্ষায় ভ্রাতৃত্ব কাফেলা • কোড: TAHAJ7
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-[#E5DFD1] flex items-center justify-between">
            <span className="text-xs bg-[#EBF4F0] text-[#123C2D] font-semibold px-2.5 py-0.5 rounded-full border border-[#123C2D]/20">৪ জন সদস্য</span>
            <span className="text-xs text-[#123C2D] font-bold flex items-center">
              প্রবেশ <ChevronRight className="w-3.5 h-3.5 ml-0.5" />
            </span>
          </div>
        </Link>

        {/* Extended Amal Card */}
        <Link href="/amal" className="bg-[#FFFDF5] hover:bg-[#FBF6E9] transition rounded-3xl p-5 border border-[#E5DFD1] shadow-sm flex flex-col justify-between group text-[#252B27]">
          <div>
            <div className="flex items-center space-x-2.5">
              <div className="w-10 h-10 rounded-2xl bg-[#FBF6E9] text-[#A68532] flex items-center justify-center border border-[#D4AF57]/40">
                <BookOpen className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-[#123C2D] group-hover:text-[#1A5641] transition">দৈনন্দিন আমল ট্র্যাকার</h3>
                <span className="text-[10px] text-[#6B7870]">কুরআন, আযকার ও সাদাকাহ</span>
              </div>
            </div>
            <p className="text-xs text-[#434E47] mt-3 leading-relaxed">
              দৈনিক কুরআন তিলাওয়াত, সকাল-সন্ধ্যার মাসনূন দু'আ ও দান
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-[#E5DFD1] flex items-center justify-between">
            <span className="text-xs bg-[#EAF5EE] text-[#1E7E52] font-semibold px-2.5 py-0.5 rounded-full border border-[#1E7E52]/20">৪টি আমল সক্রিয়</span>
            <span className="text-xs text-[#123C2D] font-bold flex items-center">
              আমল লগ <ChevronRight className="w-3.5 h-3.5 ml-0.5" />
            </span>
          </div>
        </Link>

        {/* Quick Dhikr Counter in Emerald Green & Royal Gold */}
        <div className="bg-gradient-to-br from-[#0C291E] to-[#123C2D] text-[#FFFDF5] rounded-3xl p-5 border border-[#D4AF57]/50 shadow-md flex items-center justify-between">
          <div>
            <div className="flex items-center space-x-2">
              <Fingerprint className="w-5 h-5 text-[#D4AF57]" />
              <h3 className="font-bold text-[#D4AF57]">সুবহানাল্লাহ</h3>
            </div>
            <p className="text-xs text-[#D8E2DC] mt-1">দৈনিক তাসবীহ ফাতেমী</p>
            <span className="text-xs text-[#D8E2DC] block mt-1">লক্ষ্য: ৩৩ বার</span>
          </div>
          <button
            onClick={() => setDhikrCount(c => (c + 1) % 34)}
            className="w-14 h-14 rounded-2xl bg-[#1A5641] border-2 border-[#D4AF57] flex flex-col items-center justify-center hover:bg-[#123C2D] active:scale-95 transition shadow-lg"
          >
            <span className="text-lg font-black text-[#FFFDF5]">{dhikrCount}</span>
            <span className="text-[9px] text-[#E5C678] uppercase font-bold tracking-wider">ট্যাপ</span>
          </button>
        </div>
      </div>
    </div>
  );
}
