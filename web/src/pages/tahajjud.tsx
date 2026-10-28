import React, { useState, useEffect } from 'react';
import { Moon, Save, CheckCircle2, Sparkles, Clock } from 'lucide-react';
import { getNextUpcomingTahajjud, formatTimeAmPm } from '@/lib/prayerCalculator';

export default function TahajjudPage() {
  const [performed, setPerformed] = useState(true);
  const [rakahs, setRakahs] = useState(8);
  const [startTime, setStartTime] = useState('04:10 AM');
  const [endTime, setEndTime] = useState('04:45 AM');
  const [notes, setNotes] = useState('সূরা মুলক ও সূরা সাজদাহ পাঠ করেছি');
  const [savedMessage, setSavedMessage] = useState(false);

  const [tahajjudInfo, setTahajjudInfo] = useState({ fajr: '04:45 AM', tahajjud: '04:15 AM' });

  useEffect(() => {
    const next = getNextUpcomingTahajjud();
    setTahajjudInfo({
      fajr: formatTimeAmPm(next.fajr),
      tahajjud: formatTimeAmPm(next.tahajjud)
    });
  }, []);

  const handleSave = () => {
    setSavedMessage(true);
    setTimeout(() => setSavedMessage(false), 3000);
  };

  return (
    <div className="space-y-6">
      {/* Header in Emerald Green & Royal Gold */}
      <div className="bg-gradient-to-r from-[#0C291E] via-[#123C2D] to-[#1A5641] rounded-3xl p-6 md:p-8 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-[#D4AF57]">
            <Moon className="w-5 h-5 text-[#D4AF57]" />
            <span className="text-xs font-semibold uppercase tracking-wider">কিয়ামুল লাইল ট্র্যাকার</span>
          </div>
          <h1 className="text-2xl font-black text-[#FFFDF5] mt-1">তাহাজ্জুদ সালাত ট্র্যাকার</h1>
          <p className="text-sm text-[#D8E2DC]">রাতের শেষ প্রহরে মহান আল্লাহর নৈকট্য লাভ ও হিসাব</p>
        </div>
      </div>

      {/* Schedule Banner in Deep Emerald Green #123C2D */}
      <div className="bg-[#123C2D] rounded-3xl p-6 text-[#FFFDF5] border border-[#D4AF57]/40 shadow-md">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="bg-[#0C291E]/90 rounded-2xl p-4 border border-[#D4AF57]/20">
            <span className="text-xs text-[#D8E2DC]">পরবর্তী ফজর সময়:</span>
            <p className="text-2xl font-bold text-[#FFFDF5] mt-1">{tahajjudInfo.fajr}</p>
          </div>
          <div className="bg-[#0C291E]/90 rounded-2xl p-4 border border-[#D4AF57]/20 sm:text-right">
            <span className="text-xs text-[#D8E2DC]">তাহাজ্জুদ অ্যালার্ম (৩০ মিনিট পূর্বে):</span>
            <p className="text-2xl font-bold text-[#D4AF57] mt-1">{tahajjudInfo.tahajjud}</p>
          </div>
        </div>
      </div>

      {/* Entry Form in Warm White #FFFDF5 */}
      <div className="bg-[#FFFDF5] rounded-3xl p-6 md:p-8 border border-[#E5DFD1] shadow-sm space-y-6 text-[#252B27]">
        <h2 className="text-lg font-bold text-[#123C2D]">তাহাজ্জুদের আমল রেকর্ড করুন</h2>

        {/* Performed Switch */}
        <div className="flex items-center justify-between p-4 rounded-2xl border border-[#E5DFD1] bg-[#F7F5EC]">
          <div>
            <span className="font-bold text-[#123C2D] block text-sm">
              {performed ? 'আলহামদুলিল্লাহ, তাহাজ্জুদ আদায় করেছি' : 'তাহাজ্জুদ আদায় করা হয়নি'}
            </span>
            <p className="text-xs text-[#6B7870] mt-0.5">অনিয়মিত হলেও প্রতিদিনের অবস্থা রেকর্ড রাখুন</p>
          </div>
          <button
            onClick={() => setPerformed(!performed)}
            className={`w-14 h-8 flex items-center rounded-full p-1 transition ${
              performed ? 'bg-[#123C2D] justify-end' : 'bg-[#E5DFD1] justify-start'
            }`}
          >
            <div className="w-6 h-6 rounded-full bg-[#FFFDF5] shadow-md"></div>
          </button>
        </div>

        {performed && (
          <div className="space-y-5 pt-2">
            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-2">রাকাত সংখ্যা:</label>
              <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                {[2, 4, 6, 8, 10, 12].map(r => (
                  <button
                    key={r}
                    onClick={() => setRakahs(r)}
                    className={`py-2.5 text-sm font-bold rounded-xl border transition ${
                      rakahs === r
                        ? 'bg-[#123C2D] text-[#FFFDF5] border-[#123C2D] shadow'
                        : 'bg-[#F2EFE4] text-[#252B27] border-[#E5DFD1] hover:bg-[#E5DFD1]'
                    }`}
                  >
                    {r} রাকাত
                  </button>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-[#434E47] mb-1.5">শুরুর সময়:</label>
                <input
                  type="text"
                  value={startTime}
                  onChange={e => setStartTime(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-[#434E47] mb-1.5">শেষের সময়:</label>
                <input
                  type="text"
                  value={endTime}
                  onChange={e => setEndTime(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-[#434E47] mb-1.5">ব্যক্তিগত অনুভূতি ও দু'আ (শুধুমাত্র আপনার কাছে সংরক্ষিত):</label>
              <textarea
                value={notes}
                onChange={e => setNotes(e.target.value)}
                rows={2}
                className="w-full px-4 py-3 rounded-2xl border border-[#E5DFD1] bg-white text-sm text-[#252B27] outline-none focus:outline-none focus:ring-0 focus:border-[#252B27] transition-colors duration-150"
              />
            </div>
          </div>
        )}

        <button
          onClick={handleSave}
          className="w-full py-3 bg-[#123C2D] hover:bg-[#0C291E] text-[#FFFDF5] font-bold rounded-2xl flex items-center justify-center space-x-2 transition shadow-md"
        >
          <Save className="w-4 h-4 text-[#D4AF57]" />
          <span>তাহাজ্জুদ রেকর্ড সংরক্ষণ করুন</span>
        </button>

        {savedMessage && (
          <div className="p-3 bg-[#EAF5EE] text-[#1E7E52] text-center text-xs font-semibold rounded-xl border border-[#1E7E52]/30 flex items-center justify-center space-x-2">
            <CheckCircle2 className="w-4 h-4 text-[#1E7E52]" />
            <span>তাহাজ্জুদের আমল সফলভাবে সংরক্ষিত হয়েছে!</span>
          </div>
        )}
      </div>

      {/* Lifetime Stats in Warm White Cards */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-[#FFFDF5] border border-[#E5DFD1] rounded-3xl p-5 text-center shadow-sm">
          <span className="text-3xl font-black text-[#123C2D]">২৪</span>
          <span className="block text-xs text-[#6B7870] mt-1 font-semibold">মোট তাহাজ্জুদ রাত</span>
        </div>
        <div className="bg-[#FFFDF5] border border-[#D4AF57]/40 rounded-3xl p-5 text-center shadow-sm">
          <span className="text-3xl font-black text-[#A68532]">১৯২</span>
          <span className="block text-xs text-[#6B7870] mt-1 font-semibold">সর্বমোট রাকাত আদায়</span>
        </div>
      </div>
    </div>
  );
}
